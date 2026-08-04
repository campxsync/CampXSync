/**
 * =====================================================================
 *  CampSync — MongoDB Atlas Provisioning Script
 *  Version: 2.0
 *  Companion to: CampSync_MongoDB_Architecture_v2.0.docx
 *
 *  WHAT THIS DOES
 *  - Creates every collection listed in the platform-wide collection map
 *  - Applies full $jsonSchema validators + indexes for ALL 13 core
 *    modules: Admin Manager, Academic Management, Examination
 *    Management, Finance & Accounts, Learning Management (LMS), Human
 *    Resource Management, Library Management, Hostel Management,
 *    Placement Management, Transport Management, Facilities
 *    Management, Alumni Management, Communication & Announcement.
 *    (v1.0 of this script only fully covered the first four; v2.0
 *    extends the same treatment to the remaining nine, matching the
 *    v2.0 architecture document.)
 *  - Creates platform-wide support collections: counters, outbox_events
 *  - Prints a summary at the end
 *
 *  HOW TO RUN
 *  1. In Atlas: Database → Connect → "Shell" (or use mongosh locally)
 *     mongosh "<your-atlas-connection-string>" --file campsync_provision.js
 *  2. Or paste the whole file into the Atlas Data Explorer's Shell tab.
 *  3. Re-running is safe — every step checks for existence first
 *     (idempotent). Nothing here drops or deletes existing data.
 *
 *  WHAT THIS DOES NOT DO (see the "SHARDING" section at the bottom)
 *  - Sharding requires a sharded cluster tier (Atlas Global Cluster /
 *    dedicated M30+ with sharding enabled) and is commented out by
 *    default. Uncomment that section only once you're on a tier that
 *    supports sh.shardCollection().
 *  - It does not create database users/roles — set those up under
 *    Atlas → Database Access, scoped per-module as described in
 *    Section 13.2 of the architecture document.
 *
 *  Adjust DB_NAME below before running.
 * =====================================================================
 */

const DB_NAME = "campxsync"; // change per-tenant if using dedicated-DB tenancy
// NOTE: plain reassignment (not `const`/`let`) — mongosh's global `db` would
// otherwise be shadowed by a temporal-dead-zone binding of the same name.
db = db.getSiblingDB(DB_NAME);

// ---------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------

function collectionExists(name) {
  return db.getCollectionNames().indexOf(name) !== -1;
}

/**
 * Creates a collection with a $jsonSchema validator if it doesn't
 * already exist. If it exists, leaves data alone but updates the
 * validator via collMod so re-runs pick up schema refinements.
 */
function createOrUpdateCollection(name, schema, opts = {}) {
  const validator = schema ? { $jsonSchema: schema } : undefined;
  const validationLevel = opts.validationLevel || "moderate";
  const validationAction = opts.validationAction || "warn";

  if (!collectionExists(name)) {
    const cmdOpts = {};
    if (validator) {
      cmdOpts.validator = validator;
      cmdOpts.validationLevel = validationLevel;
      cmdOpts.validationAction = validationAction;
    }
    db.createCollection(name, cmdOpts);
    print("  [created] " + name);
  } else {
    if (validator) {
      db.runCommand({
        collMod: name,
        validator: validator,
        validationLevel: validationLevel,
        validationAction: validationAction,
      });
      print("  [exists -> validator updated] " + name);
    } else {
      print("  [exists -> left as-is] " + name);
    }
  }
}

/**
 * Creates an index if an index with the same key spec doesn't already
 * exist. Cheap idempotency check by comparing key signatures.
 */
function ensureIndex(name, keys, opts = {}) {
  const existing = db.getCollection(name).getIndexes();
  const wanted = JSON.stringify(keys);
  const already = existing.some((ix) => JSON.stringify(ix.key) === wanted);
  if (!already) {
    db.getCollection(name).createIndex(keys, opts);
    print("  [index created] " + name + " " + wanted + (opts.name ? " (" + opts.name + ")" : ""));
  } else {
    print("  [index exists] " + name + " " + wanted);
  }
}

print("=====================================================");
print(" CampSync MongoDB provisioning — database: " + DB_NAME);
print("=====================================================");

// =====================================================================
// MODULE 12 — ADMIN MANAGER  (users, roles, permissions, RBAC, audit)
// =====================================================================
print("\n--- Admin Manager ---");

createOrUpdateCollection("users", {
  bsonType: "object",
  required: ["institution_id", "auth", "profile_type", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    auth: {
      bsonType: "object",
      required: ["email"],
      properties: {
        email: { bsonType: "string" },
        phone: { bsonType: "string" },
        password_hash: { bsonType: "string" },
        sso_provider: { bsonType: "string" },
        mfa_enabled: { bsonType: "bool" },
      },
    },
    profile_type: { enum: ["student", "faculty", "staff", "parent", "alumni", "admin"] },
    profile: { bsonType: "object" },
    status: { enum: ["active", "suspended", "deactivated"] },
    created_at: { bsonType: "date" },
    updated_at: { bsonType: "date" },
  },
});
ensureIndex("users", { institution_id: 1, "auth.email": 1 }, { unique: true, name: "uniq_inst_email" });
ensureIndex("users", { institution_id: 1, profile_type: 1, status: 1 }, { name: "idx_inst_profiletype_status" });

createOrUpdateCollection("roles", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    description: { bsonType: "string" },
  },
});
ensureIndex("roles", { institution_id: 1, name: 1 }, { unique: true, name: "uniq_inst_role_name" });

createOrUpdateCollection("permissions", {
  bsonType: "object",
  required: ["resource", "action"],
  properties: {
    resource: { bsonType: "string" },
    action: { bsonType: "string" },
    description: { bsonType: "string" },
  },
});
ensureIndex("permissions", { resource: 1, action: 1 }, { unique: true, name: "uniq_resource_action" });

createOrUpdateCollection("role_assignments", {
  bsonType: "object",
  required: ["institution_id", "user_id", "role_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    user_id: { bsonType: "objectId" },
    role_id: { bsonType: "objectId" },
    scope: {
      bsonType: "object",
      properties: {
        scope_type: { bsonType: "string" },
        scope_id: { bsonType: "objectId" },
      },
    },
    granted_by: { bsonType: "objectId" },
    granted_at: { bsonType: "date" },
    expires_at: { bsonType: "date" },
  },
});
ensureIndex("role_assignments", { institution_id: 1, user_id: 1 }, { name: "idx_inst_user" });
ensureIndex("role_assignments", { institution_id: 1, role_id: 1, "scope.scope_id": 1 }, { name: "idx_inst_role_scope" });

createOrUpdateCollection("system_configs", {
  bsonType: "object",
  required: ["institution_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    feature_flags: { bsonType: "object" },
    branding: { bsonType: "object" },
    integration_keys: { bsonType: "object" },
  },
});
ensureIndex("system_configs", { institution_id: 1 }, { unique: true, name: "uniq_inst" });

createOrUpdateCollection("audit_logs", {
  bsonType: "object",
  required: ["institution_id", "event_type", "occurred_at"],
  properties: {
    institution_id: { bsonType: "objectId" },
    event_type: { bsonType: "string" },
    actor: { bsonType: "object" },
    target: { bsonType: "object" },
    before: { bsonType: "object" },
    after: { bsonType: "object" },
    occurred_at: { bsonType: "date" },
    source: { bsonType: "string" },
  },
}, { validationAction: "warn" }); // warn, not error: never block an audit write
ensureIndex("audit_logs", { institution_id: 1, occurred_at: -1 }, { name: "idx_inst_time" });
ensureIndex("audit_logs", { institution_id: 1, "target.document_id": 1 }, { name: "idx_inst_target" });
ensureIndex("audit_logs", { institution_id: 1, event_type: 1, occurred_at: -1 }, { name: "idx_inst_eventtype_time" });

// Platform-wide support collections
createOrUpdateCollection("counters", {
  bsonType: "object",
  required: ["_id", "seq"],
  properties: { _id: { bsonType: "string" }, seq: { bsonType: "long" } },
});

createOrUpdateCollection("outbox_events", {
  bsonType: "object",
  required: ["institution_id", "event_type", "payload", "published"],
  properties: {
    institution_id: { bsonType: "objectId" },
    event_type: { bsonType: "string" },
    payload: { bsonType: "object" },
    published: { bsonType: "bool" },
    created_at: { bsonType: "date" },
    published_at: { bsonType: "date" },
  },
});
ensureIndex("outbox_events", { published: 1, created_at: 1 }, { name: "idx_published_created" });
// =====================================================================
// MODULE 1 — ACADEMIC MANAGEMENT
// =====================================================================
print("\n--- Academic Management ---");

createOrUpdateCollection("programs", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    course_ids: { bsonType: "array", items: { bsonType: "objectId" } },
  },
});
ensureIndex("programs", { institution_id: 1, name: 1 }, { unique: true, name: "uniq_inst_name" });

createOrUpdateCollection("courses", {
  bsonType: "object",
  required: ["institution_id", "code", "title"],
  properties: {
    institution_id: { bsonType: "objectId" },
    program_id: { bsonType: "objectId" },
    code: { bsonType: "string" },
    title: { bsonType: "string" },
    curriculum: { bsonType: "array" }, // embedded unit sub-documents
  },
});
ensureIndex("courses", { institution_id: 1, code: 1 }, { unique: true, name: "uniq_inst_code" });
ensureIndex("courses", { institution_id: 1, program_id: 1 }, { name: "idx_inst_program" });

createOrUpdateCollection("course_sections", {
  bsonType: "object",
  required: ["institution_id", "course_id", "term_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    course_id: { bsonType: "objectId" },
    term_id: { bsonType: "objectId" },
    faculty_id: { bsonType: "objectId" },
    capacity: { bsonType: "int" },
    seats_filled: { bsonType: "int" },
    schedule_ids: { bsonType: "array", items: { bsonType: "objectId" } },
    status: { enum: ["draft", "open", "closed", "archived"] },
  },
});
ensureIndex("course_sections", { institution_id: 1, term_id: 1, faculty_id: 1 }, { name: "idx_inst_term_faculty" });
ensureIndex("course_sections", { institution_id: 1, course_id: 1, term_id: 1 }, { unique: true, name: "uniq_inst_course_term" });

createOrUpdateCollection("class_schedules", {
  bsonType: "object",
  required: ["institution_id", "course_section_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    course_section_id: { bsonType: "objectId" },
    day_of_week: { bsonType: "string" },
    start_time: { bsonType: "string" },
    end_time: { bsonType: "string" },
    room: { bsonType: "string" },
  },
});
ensureIndex("class_schedules", { institution_id: 1, course_section_id: 1 }, { name: "idx_inst_section" });

createOrUpdateCollection("enrollments", {
  bsonType: "object",
  required: ["institution_id", "student_id", "course_section_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    course_section_id: { bsonType: "objectId" },
    term_id: { bsonType: "objectId" },
    status: { enum: ["pending_fee", "active", "dropped", "completed"] },
    enrolled_at: { bsonType: "date" },
    fee_invoice_id: { bsonType: "objectId" },
    grade: { bsonType: "object" },
    attendance_summary: { bsonType: "object" },
    created_at: { bsonType: "date" },
    updated_at: { bsonType: "date" },
  },
}, { validationAction: "error" }); // enforce strictly: this is the platform's busiest, most cross-referenced collection
ensureIndex("enrollments", { institution_id: 1, student_id: 1, term_id: 1 }, { unique: true, name: "uniq_inst_student_term" });
ensureIndex("enrollments", { institution_id: 1, course_section_id: 1, status: 1 }, { name: "idx_inst_section_status" });

createOrUpdateCollection("academic_calendars", {
  bsonType: "object",
  required: ["institution_id", "term_name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    term_name: { bsonType: "string" },
    start_date: { bsonType: "date" },
    end_date: { bsonType: "date" },
    exam_window: { bsonType: "object" },
  },
});
ensureIndex("academic_calendars", { institution_id: 1, term_name: 1 }, { unique: true, name: "uniq_inst_term" });

createOrUpdateCollection("learning_pathways", {
  bsonType: "object",
  required: ["institution_id", "student_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    recommended_course_ids: { bsonType: "array", items: { bsonType: "objectId" } },
    generated_by: { bsonType: "string" }, // e.g. "insight_engine"
  },
});
ensureIndex("learning_pathways", { institution_id: 1, student_id: 1 }, { name: "idx_inst_student" });
// =====================================================================
// MODULE 2 — EXAMINATION MANAGEMENT
// =====================================================================
print("\n--- Examination Management ---");

createOrUpdateCollection("exam_registrations", {
  bsonType: "object",
  required: ["institution_id", "student_id", "course_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    course_id: { bsonType: "objectId" },
    exam_timetable_id: { bsonType: "objectId" },
    registered_at: { bsonType: "date" },
  },
});
ensureIndex("exam_registrations", { institution_id: 1, student_id: 1, course_id: 1 }, { unique: true, name: "uniq_inst_student_course" });

createOrUpdateCollection("exam_timetables", {
  bsonType: "object",
  required: ["institution_id", "term_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    term_id: { bsonType: "objectId" },
    sessions: { bsonType: "array" },
  },
});
ensureIndex("exam_timetables", { institution_id: 1, term_id: 1 }, { name: "idx_inst_term" });

createOrUpdateCollection("hall_tickets", {
  bsonType: "object",
  required: ["institution_id", "exam_registration_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    exam_registration_id: { bsonType: "objectId" },
    seat_number: { bsonType: "string" },
    venue: { bsonType: "string" },
    session_slot: { bsonType: "string" },
    document_ref: {
      bsonType: "object",
      properties: { bucket: { bsonType: "string" }, key: { bsonType: "string" }, checksum: { bsonType: "string" } },
    },
    eligibility_check: {
      bsonType: "object",
      properties: {
        fee_cleared: { bsonType: "bool" },
        attendance_cleared: { bsonType: "bool" },
        checked_at: { bsonType: "date" },
      },
    },
  },
});
ensureIndex("hall_tickets", { institution_id: 1, exam_registration_id: 1 }, { unique: true, name: "uniq_inst_examreg" });

createOrUpdateCollection("question_banks", {
  bsonType: "object",
  required: ["institution_id", "course_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    course_id: { bsonType: "objectId" },
    topic: { bsonType: "string" },
    difficulty: { enum: ["easy", "medium", "hard"] },
    question_text: { bsonType: "string" },
  },
});
ensureIndex("question_banks", { institution_id: 1, course_id: 1, topic: 1 }, { name: "idx_inst_course_topic" });

createOrUpdateCollection("exam_results", {
  bsonType: "object",
  required: ["institution_id", "student_id", "course_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    exam_registration_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    course_id: { bsonType: "objectId" },
    marks_breakdown: { bsonType: "array" },
    total_obtained: { bsonType: ["double", "int"] },
    total_max: { bsonType: ["double", "int"] },
    grade_letter: { bsonType: "string" },
    grade_points: { bsonType: ["double", "int"] },
    status: { enum: ["draft", "moderation", "published", "under_revaluation", "finalized", "superseded"] },
    published_at: { bsonType: "date" },
    revision_of: { bsonType: "objectId" },
  },
}, { validationAction: "error" }); // append-only ledger — see architecture doc Section 5.2
ensureIndex("exam_results", { institution_id: 1, student_id: 1, course_id: 1, status: 1 }, { name: "idx_inst_student_course_status" });
ensureIndex(
  "exam_results",
  { institution_id: 1, exam_registration_id: 1 },
  {
    unique: true,
    name: "uniq_inst_examreg_current",
    partialFilterExpression: { status: { $in: ["published", "finalized"] } },
  }
);

createOrUpdateCollection("revaluation_requests", {
  bsonType: "object",
  required: ["institution_id", "exam_result_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    exam_result_id: { bsonType: "objectId" },
    status: { enum: ["requested", "in_review", "approved", "rejected"] },
    requested_at: { bsonType: "date" },
  },
});
ensureIndex("revaluation_requests", { institution_id: 1, exam_result_id: 1 }, { name: "idx_inst_result" });

createOrUpdateCollection("certificates", {
  bsonType: "object",
  required: ["institution_id", "student_id", "type"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    type: { bsonType: "string" }, // marksheet | provisional | degree | transcript
    document_ref: { bsonType: "object" },
    issued_at: { bsonType: "date" },
  },
});
ensureIndex("certificates", { institution_id: 1, student_id: 1, type: 1 }, { name: "idx_inst_student_type" });
// =====================================================================
// MODULE 4 — FINANCE & ACCOUNTS
// =====================================================================
print("\n--- Finance & Accounts ---");

createOrUpdateCollection("fee_structures", {
  bsonType: "object",
  required: ["institution_id", "program_id", "term_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    program_id: { bsonType: "objectId" },
    term_id: { bsonType: "objectId" },
    heads: { bsonType: "array" }, // [{head, amount}]
  },
});
ensureIndex("fee_structures", { institution_id: 1, program_id: 1, term_id: 1 }, { unique: true, name: "uniq_inst_program_term" });

createOrUpdateCollection("fee_invoices", {
  bsonType: "object",
  required: ["institution_id", "student_id", "total_amount", "status", "version"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    enrollment_id: { bsonType: "objectId" },
    fee_structure_id: { bsonType: "objectId" },
    line_items: { bsonType: "array" },
    total_amount: { bsonType: "decimal" },
    amount_paid: { bsonType: "decimal" },
    balance: { bsonType: "decimal" },
    status: { enum: ["issued", "partially_paid", "paid", "overdue", "waived", "cancelled"] },
    due_date: { bsonType: "date" },
    version: { bsonType: "int" },
  },
}, { validationAction: "error" }); // money — strict validation
ensureIndex("fee_invoices", { institution_id: 1, student_id: 1, status: 1, due_date: 1 }, { name: "idx_inst_student_status_due" });
ensureIndex("fee_invoices", { institution_id: 1, enrollment_id: 1 }, { name: "idx_inst_enrollment" });
ensureIndex(
  "fee_invoices",
  { institution_id: 1, status: 1 },
  { name: "idx_outstanding_partial", partialFilterExpression: { status: { $in: ["issued", "partially_paid", "overdue"] } } }
);

createOrUpdateCollection("fee_receipts", {
  bsonType: "object",
  required: ["institution_id", "fee_invoice_id", "amount", "idempotency_key"],
  properties: {
    institution_id: { bsonType: "objectId" },
    fee_invoice_id: { bsonType: "objectId" },
    amount: { bsonType: "decimal" },
    payment_method: { enum: ["card", "upi", "netbanking", "dd", "cash"] },
    gateway_reference: { bsonType: "object" },
    receipt_number: { bsonType: "string" },
    idempotency_key: { bsonType: "string" },
    created_at: { bsonType: "date" },
  },
}, { validationAction: "error" });
ensureIndex("fee_receipts", { institution_id: 1, idempotency_key: 1 }, { unique: true, name: "uniq_inst_idempotency" });
ensureIndex("fee_receipts", { institution_id: 1, fee_invoice_id: 1 }, { name: "idx_inst_invoice" });

createOrUpdateCollection("scholarships", {
  bsonType: "object",
  required: ["institution_id", "student_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    type: { bsonType: "string" },
    amount: { bsonType: "decimal" },
    awarded_at: { bsonType: "date" },
  },
});
ensureIndex("scholarships", { institution_id: 1, student_id: 1 }, { name: "idx_inst_student" });

createOrUpdateCollection("expenses", {
  bsonType: "object",
  required: ["institution_id", "department_id", "amount"],
  properties: {
    institution_id: { bsonType: "objectId" },
    department_id: { bsonType: "objectId" },
    amount: { bsonType: "decimal" },
    category: { bsonType: "string" },
    incurred_at: { bsonType: "date" },
  },
});
ensureIndex("expenses", { institution_id: 1, department_id: 1, incurred_at: -1 }, { name: "idx_inst_dept_time" });

createOrUpdateCollection("budgets", {
  bsonType: "object",
  required: ["institution_id", "department_id", "fiscal_year"],
  properties: {
    institution_id: { bsonType: "objectId" },
    department_id: { bsonType: "objectId" },
    fiscal_year: { bsonType: "string" },
    allocated: { bsonType: "decimal" },
    utilized: { bsonType: "decimal" },
  },
});
ensureIndex("budgets", { institution_id: 1, department_id: 1, fiscal_year: 1 }, { unique: true, name: "uniq_inst_dept_fy" });

createOrUpdateCollection("financial_reports", {
  bsonType: "object",
  required: ["institution_id", "report_type"],
  properties: {
    institution_id: { bsonType: "objectId" },
    report_type: { bsonType: "string" },
    period: { bsonType: "string" },
    document_ref: { bsonType: "object" },
    generated_at: { bsonType: "date" },
  },
});
ensureIndex("financial_reports", { institution_id: 1, report_type: 1, period: 1 }, { name: "idx_inst_type_period" });
// =====================================================================
// MODULE 3 — LEARNING MANAGEMENT (LMS)
// =====================================================================
print("\n--- Learning Management (LMS) ---");

createOrUpdateCollection("course_content", {
  bsonType: "object",
  required: ["institution_id", "course_section_id", "title"],
  properties: {
    institution_id: { bsonType: "objectId" },
    course_section_id: { bsonType: "objectId" },
    title: { bsonType: "string" },
    type: { bsonType: "string" }, // video | document | link | quiz
    content_ref: { bsonType: "object" }, // { bucket, key, checksum }
    order: { bsonType: "int" },
  },
});
ensureIndex("course_content", { institution_id: 1, course_section_id: 1, order: 1 }, { name: "idx_inst_section_order" });

createOrUpdateCollection("assignments", {
  bsonType: "object",
  required: ["institution_id", "course_section_id", "title"],
  properties: {
    institution_id: { bsonType: "objectId" },
    course_section_id: { bsonType: "objectId" },
    title: { bsonType: "string" },
    due_date: { bsonType: "date" },
    max_score: { bsonType: ["double", "int"] },
  },
});
ensureIndex("assignments", { institution_id: 1, course_section_id: 1 }, { name: "idx_inst_section" });

createOrUpdateCollection("submissions", {
  bsonType: "object",
  required: ["institution_id", "assignment_id", "student_id", "attempt_number", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    assignment_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    content_ref: { bsonType: "object" },
    submitted_at: { bsonType: "date" },
    attempt_number: { bsonType: "int" },
    grading: {
      bsonType: "object",
      properties: {
        score: { bsonType: ["double", "int"] },
        max_score: { bsonType: ["double", "int"] },
        feedback: { bsonType: "string" },
        graded_by: { bsonType: "objectId" },
        graded_at: { bsonType: "date" },
      },
    },
    status: { enum: ["submitted", "late", "graded", "resubmission_requested"] },
  },
}, { validationAction: "error" });
ensureIndex("submissions", { institution_id: 1, assignment_id: 1, student_id: 1, attempt_number: 1 }, { unique: true, name: "uniq_inst_assignment_student_attempt" });
ensureIndex("submissions", { institution_id: 1, student_id: 1 }, { name: "idx_inst_student" });

createOrUpdateCollection("discussion_threads", {
  bsonType: "object",
  required: ["institution_id", "course_section_id", "title"],
  properties: {
    institution_id: { bsonType: "objectId" },
    course_section_id: { bsonType: "objectId" },
    title: { bsonType: "string" },
    created_by: { bsonType: "objectId" },
    created_at: { bsonType: "date" },
  },
});
ensureIndex("discussion_threads", { institution_id: 1, course_section_id: 1 }, { name: "idx_inst_section" });

// Replies modeled as separate documents (not embedded — see architecture doc §15.4)
createOrUpdateCollection("discussion_replies", {
  bsonType: "object",
  required: ["institution_id", "thread_id", "author_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    thread_id: { bsonType: "objectId" },
    author_id: { bsonType: "objectId" },
    body: { bsonType: "string" },
    posted_at: { bsonType: "date" },
  },
});
ensureIndex("discussion_replies", { institution_id: 1, thread_id: 1, posted_at: 1 }, { name: "idx_inst_thread_time" });

createOrUpdateCollection("virtual_classrooms", {
  bsonType: "object",
  required: ["institution_id", "course_section_id", "scheduled_at"],
  properties: {
    institution_id: { bsonType: "objectId" },
    course_section_id: { bsonType: "objectId" },
    scheduled_at: { bsonType: "date" },
    recording_ref: { bsonType: "object" },
    status: { enum: ["scheduled", "live", "completed", "cancelled"] },
  },
});
ensureIndex("virtual_classrooms", { institution_id: 1, course_section_id: 1, scheduled_at: 1 }, { name: "idx_inst_section_time" });

createOrUpdateCollection("progress_tracking", {
  bsonType: "object",
  required: ["institution_id", "student_id", "course_section_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    course_section_id: { bsonType: "objectId" },
    completed_content_ids: { bsonType: "array", items: { bsonType: "objectId" } },
    completion_percentage: { bsonType: ["double", "int"] },
    last_activity_at: { bsonType: "date" },
  },
});
ensureIndex("progress_tracking", { institution_id: 1, student_id: 1, course_section_id: 1 }, { unique: true, name: "uniq_inst_student_section" });

createOrUpdateCollection("badges", {
  bsonType: "object",
  required: ["institution_id", "student_id", "badge_type"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    badge_type: { bsonType: "string" },
    awarded_at: { bsonType: "date" },
  },
});
ensureIndex("badges", { institution_id: 1, student_id: 1 }, { name: "idx_inst_student" });
// =====================================================================
// MODULE 5 — HUMAN RESOURCE MANAGEMENT
// =====================================================================
print("\n--- Human Resource Management ---");

createOrUpdateCollection("employees", {
  bsonType: "object",
  required: ["institution_id", "user_id", "employee_code", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    user_id: { bsonType: "objectId" },
    employee_code: { bsonType: "string" },
    department_id: { bsonType: "objectId" },
    designation: { bsonType: "string" },
    employment_type: { enum: ["permanent", "contract", "visiting", "probation"] },
    date_of_joining: { bsonType: "date" },
    reporting_to: { bsonType: "objectId" },
    status: { enum: ["active", "on_leave", "separated"] },
  },
});
ensureIndex("employees", { institution_id: 1, employee_code: 1 }, { unique: true, name: "uniq_inst_empcode" });
ensureIndex("employees", { institution_id: 1, department_id: 1 }, { name: "idx_inst_dept" });

createOrUpdateCollection("onboarding_cases", {
  bsonType: "object",
  required: ["institution_id", "employee_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    employee_id: { bsonType: "objectId" },
    checklist: { bsonType: "array" },
    status: { enum: ["pending", "in_progress", "completed"] },
  },
});
ensureIndex("onboarding_cases", { institution_id: 1, employee_id: 1 }, { unique: true, name: "uniq_inst_employee" });

createOrUpdateCollection("leave_requests", {
  bsonType: "object",
  required: ["institution_id", "employee_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    employee_id: { bsonType: "objectId" },
    leave_type: { bsonType: "string" },
    start_date: { bsonType: "date" },
    end_date: { bsonType: "date" },
    status: { enum: ["requested", "approved", "rejected", "cancelled"] },
  },
});
ensureIndex("leave_requests", { institution_id: 1, employee_id: 1, status: 1 }, { name: "idx_inst_employee_status" });

createOrUpdateCollection("attendance_timesheets", {
  bsonType: "object",
  required: ["institution_id", "employee_id", "date"],
  properties: {
    institution_id: { bsonType: "objectId" },
    employee_id: { bsonType: "objectId" },
    date: { bsonType: "date" },
    hours_worked: { bsonType: ["double", "int"] },
    status: { enum: ["present", "absent", "half_day", "on_leave"] },
  },
});
ensureIndex("attendance_timesheets", { institution_id: 1, employee_id: 1, date: 1 }, { unique: true, name: "uniq_inst_employee_date" });

createOrUpdateCollection("performance_reviews", {
  bsonType: "object",
  required: ["institution_id", "employee_id", "review_period"],
  properties: {
    institution_id: { bsonType: "objectId" },
    employee_id: { bsonType: "objectId" },
    review_period: { bsonType: "string" },
    rating: { bsonType: ["double", "int"] },
    reviewed_by: { bsonType: "objectId" },
  },
});
ensureIndex("performance_reviews", { institution_id: 1, employee_id: 1, review_period: 1 }, { unique: true, name: "uniq_inst_employee_period" });

createOrUpdateCollection("training_records", {
  bsonType: "object",
  required: ["institution_id", "employee_id", "training_name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    employee_id: { bsonType: "objectId" },
    training_name: { bsonType: "string" },
    completed_at: { bsonType: "date" },
    status: { enum: ["enrolled", "in_progress", "completed"] },
  },
});
ensureIndex("training_records", { institution_id: 1, employee_id: 1 }, { name: "idx_inst_employee" });

createOrUpdateCollection("payroll_runs", {
  bsonType: "object",
  required: ["institution_id", "employee_id", "period", "gross_amount", "net_amount", "status", "idempotency_key"],
  properties: {
    institution_id: { bsonType: "objectId" },
    employee_id: { bsonType: "objectId" },
    period: { bsonType: "string" },
    line_items: { bsonType: "array" },
    gross_amount: { bsonType: "decimal" },
    net_amount: { bsonType: "decimal" },
    status: { enum: ["draft", "approved", "disbursed", "reversed"] },
    disbursed_at: { bsonType: "date" },
    idempotency_key: { bsonType: "string" },
  },
}, { validationAction: "error" }); // money — strict validation, same convention as Finance
ensureIndex("payroll_runs", { institution_id: 1, idempotency_key: 1 }, { unique: true, name: "uniq_inst_idempotency" });
ensureIndex("payroll_runs", { institution_id: 1, employee_id: 1, period: 1 }, { unique: true, name: "uniq_inst_employee_period" });
// =====================================================================
// MODULE 6 — LIBRARY MANAGEMENT
// =====================================================================
print("\n--- Library Management ---");

createOrUpdateCollection("catalog_items", {
  bsonType: "object",
  required: ["institution_id", "title", "total_copies", "available_copies"],
  properties: {
    institution_id: { bsonType: "objectId" },
    isbn: { bsonType: "string" },
    title: { bsonType: "string" },
    authors: { bsonType: "array", items: { bsonType: "string" } },
    subject_tags: { bsonType: "array", items: { bsonType: "string" } },
    total_copies: { bsonType: "int" },
    available_copies: { bsonType: "int" },
    location: { bsonType: "string" },
  },
});
ensureIndex("catalog_items", { institution_id: 1, isbn: 1 }, { name: "idx_inst_isbn" });
ensureIndex("catalog_items", { institution_id: 1, title: 1 }, { name: "idx_inst_title" });

createOrUpdateCollection("book_circulations", {
  bsonType: "object",
  required: ["institution_id", "catalog_item_id", "member_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    catalog_item_id: { bsonType: "objectId" },
    member_id: { bsonType: "objectId" },
    issued_at: { bsonType: "date" },
    due_at: { bsonType: "date" },
    returned_at: { bsonType: "date" },
    renewal_count: { bsonType: "int" },
    status: { enum: ["issued", "returned", "overdue", "lost"] },
    fine_id: { bsonType: "objectId" },
  },
}, { validationAction: "error" }); // checkout decrements available_copies — keep strict
ensureIndex("book_circulations", { institution_id: 1, catalog_item_id: 1, status: 1 }, { name: "idx_inst_item_status" });
ensureIndex("book_circulations", { institution_id: 1, member_id: 1 }, { name: "idx_inst_member" });

createOrUpdateCollection("digital_library_assets", {
  bsonType: "object",
  required: ["institution_id", "title"],
  properties: {
    institution_id: { bsonType: "objectId" },
    title: { bsonType: "string" },
    resource_type: { bsonType: "string" },
    access_ref: { bsonType: "object" },
  },
});
ensureIndex("digital_library_assets", { institution_id: 1, title: 1 }, { name: "idx_inst_title" });

createOrUpdateCollection("memberships", {
  bsonType: "object",
  required: ["institution_id", "user_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    user_id: { bsonType: "objectId" },
    membership_type: { bsonType: "string" },
    max_books: { bsonType: "int" },
    status: { enum: ["active", "suspended", "expired"] },
  },
});
ensureIndex("memberships", { institution_id: 1, user_id: 1 }, { unique: true, name: "uniq_inst_user" });

createOrUpdateCollection("fines", {
  bsonType: "object",
  required: ["institution_id", "book_circulation_id", "amount", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    book_circulation_id: { bsonType: "objectId" },
    amount: { bsonType: "decimal" },
    status: { enum: ["outstanding", "paid", "waived"] },
    paid_at: { bsonType: "date" },
  },
});
ensureIndex("fines", { institution_id: 1, book_circulation_id: 1 }, { name: "idx_inst_circulation" });
// =====================================================================
// MODULE 7 — HOSTEL MANAGEMENT
// =====================================================================
print("\n--- Hostel Management ---");

createOrUpdateCollection("rooms", {
  bsonType: "object",
  required: ["institution_id", "room_number", "capacity"],
  properties: {
    institution_id: { bsonType: "objectId" },
    room_number: { bsonType: "string" },
    capacity: { bsonType: "int" },
    occupied_beds: { bsonType: "int" },
    hostel_block: { bsonType: "string" },
  },
});
ensureIndex("rooms", { institution_id: 1, room_number: 1 }, { unique: true, name: "uniq_inst_room" });

createOrUpdateCollection("room_allocations", {
  bsonType: "object",
  required: ["institution_id", "student_id", "room_id", "academic_year", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    room_id: { bsonType: "objectId" },
    academic_year: { bsonType: "string" },
    status: { enum: ["active", "vacated", "transferred"] },
    allocated_at: { bsonType: "date" },
    vacated_at: { bsonType: "date" },
  },
}, { validationAction: "error" }); // capacity-guarded write
ensureIndex("room_allocations", { institution_id: 1, student_id: 1, academic_year: 1 }, { unique: true, name: "uniq_inst_student_year" });
ensureIndex("room_allocations", { institution_id: 1, room_id: 1 }, { name: "idx_inst_room" });

createOrUpdateCollection("hostel_checkins", {
  bsonType: "object",
  required: ["institution_id", "student_id", "direction", "recorded_at"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    direction: { enum: ["in", "out"] },
    recorded_at: { bsonType: "date" },
    method: { bsonType: "string" },
  },
});
ensureIndex("hostel_checkins", { institution_id: 1, student_id: 1, recorded_at: -1 }, { name: "idx_inst_student_time" });

createOrUpdateCollection("mess_plans", {
  bsonType: "object",
  required: ["institution_id", "student_id", "plan_type", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    plan_type: { enum: ["veg", "non_veg", "jain", "custom"] },
    term_id: { bsonType: "objectId" },
    fee_invoice_id: { bsonType: "objectId" },
    status: { enum: ["active", "paused", "cancelled"] },
    start_date: { bsonType: "date" },
    end_date: { bsonType: "date" },
  },
});
ensureIndex("mess_plans", { institution_id: 1, student_id: 1, term_id: 1 }, { unique: true, name: "uniq_inst_student_term" });

createOrUpdateCollection("visitor_logs", {
  bsonType: "object",
  required: ["institution_id", "student_id", "visitor_name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    visitor_name: { bsonType: "string" },
    checked_in_at: { bsonType: "date" },
    checked_out_at: { bsonType: "date" },
  },
});
ensureIndex("visitor_logs", { institution_id: 1, student_id: 1 }, { name: "idx_inst_student" });

createOrUpdateCollection("maintenance_requests", {
  bsonType: "object",
  required: ["institution_id", "room_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    room_id: { bsonType: "objectId" },
    reported_by: { bsonType: "objectId" },
    status: { enum: ["open", "in_progress", "resolved", "closed"] },
    priority: { enum: ["low", "medium", "high", "critical"] },
  },
});
ensureIndex("maintenance_requests", { institution_id: 1, room_id: 1, status: 1 }, { name: "idx_inst_room_status" });
// =====================================================================
// MODULE 8 — PLACEMENT MANAGEMENT
// =====================================================================
print("\n--- Placement Management ---");

createOrUpdateCollection("companies", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    industry: { bsonType: "string" },
    contact: { bsonType: "object" },
  },
});
ensureIndex("companies", { institution_id: 1, name: 1 }, { unique: true, name: "uniq_inst_name" });

createOrUpdateCollection("placement_drives", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    term_id: { bsonType: "objectId" },
    start_date: { bsonType: "date" },
    end_date: { bsonType: "date" },
  },
});
ensureIndex("placement_drives", { institution_id: 1, term_id: 1 }, { name: "idx_inst_term" });

createOrUpdateCollection("job_postings", {
  bsonType: "object",
  required: ["institution_id", "company_id", "title"],
  properties: {
    institution_id: { bsonType: "objectId" },
    company_id: { bsonType: "objectId" },
    placement_drive_id: { bsonType: "objectId" },
    title: { bsonType: "string" },
    eligibility_criteria: { bsonType: "object" },
  },
});
ensureIndex("job_postings", { institution_id: 1, company_id: 1 }, { name: "idx_inst_company" });
ensureIndex("job_postings", { institution_id: 1, placement_drive_id: 1 }, { name: "idx_inst_drive" });

createOrUpdateCollection("student_applications", {
  bsonType: "object",
  required: ["institution_id", "student_id", "job_posting_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    job_posting_id: { bsonType: "objectId" },
    eligibility_snapshot: {
      bsonType: "object",
      properties: {
        avg_gpa: { bsonType: ["double", "int"] },
        backlog_count: { bsonType: "int" },
        checked_at: { bsonType: "date" },
      },
    },
    status: { enum: ["applied", "shortlisted", "interviewing", "offered", "rejected", "withdrawn"] },
    applied_at: { bsonType: "date" },
  },
});
ensureIndex("student_applications", { institution_id: 1, student_id: 1, job_posting_id: 1 }, { unique: true, name: "uniq_inst_student_posting" });

createOrUpdateCollection("interview_schedules", {
  bsonType: "object",
  required: ["institution_id", "student_application_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_application_id: { bsonType: "objectId" },
    round: { bsonType: "int" },
    scheduled_at: { bsonType: "date" },
    status: { enum: ["scheduled", "completed", "no_show", "rescheduled"] },
  },
});
ensureIndex("interview_schedules", { institution_id: 1, student_application_id: 1 }, { name: "idx_inst_application" });

createOrUpdateCollection("offers", {
  bsonType: "object",
  required: ["institution_id", "student_application_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    student_application_id: { bsonType: "objectId" },
    compensation: {
      bsonType: "object",
      properties: {
        ctc: { bsonType: "decimal" },
        currency: { bsonType: "string" },
        breakup: { bsonType: "object" },
      },
    },
    status: { enum: ["extended", "accepted", "declined", "expired"] },
    responded_at: { bsonType: "date" },
  },
});
ensureIndex("offers", { institution_id: 1, student_application_id: 1 }, { unique: true, name: "uniq_inst_application" });
// =====================================================================
// MODULE 9 — TRANSPORT MANAGEMENT
// =====================================================================
print("\n--- Transport Management ---");

createOrUpdateCollection("routes", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    stops: { bsonType: "array" }, // embedded bounded sub-document array — architecture doc §20.5
  },
});
ensureIndex("routes", { institution_id: 1, name: 1 }, { unique: true, name: "uniq_inst_name" });

createOrUpdateCollection("vehicles", {
  bsonType: "object",
  required: ["institution_id", "registration_number", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    registration_number: { bsonType: "string" },
    type: { enum: ["bus", "van", "car"] },
    capacity: { bsonType: "int" },
    current_assignment_id: { bsonType: "objectId" }, // denormalized pointer — architecture doc §20.4
    status: { enum: ["active", "maintenance", "retired"] },
    last_serviced_at: { bsonType: "date" },
  },
});
ensureIndex("vehicles", { institution_id: 1, registration_number: 1 }, { unique: true, name: "uniq_inst_regnum" });

createOrUpdateCollection("vehicle_assignments", {
  bsonType: "object",
  required: ["institution_id", "vehicle_id", "route_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    vehicle_id: { bsonType: "objectId" },
    route_id: { bsonType: "objectId" },
    driver_id: { bsonType: "objectId" },
    effective_from: { bsonType: "date" },
    effective_to: { bsonType: "date" },
  },
});
ensureIndex("vehicle_assignments", { institution_id: 1, vehicle_id: 1 }, { name: "idx_inst_vehicle" });
ensureIndex("vehicle_assignments", { institution_id: 1, route_id: 1 }, { name: "idx_inst_route" });

createOrUpdateCollection("driver_profiles", {
  bsonType: "object",
  required: ["institution_id", "name", "license_number"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    license_number: { bsonType: "string" },
    contact: { bsonType: "object" },
  },
});
ensureIndex("driver_profiles", { institution_id: 1, license_number: 1 }, { unique: true, name: "uniq_inst_license" });

// gps_pings — time-series collection (see architecture doc §20.2)
if (!collectionExists("gps_pings")) {
  db.createCollection("gps_pings", {
    timeseries: {
      timeField: "ts",
      metaField: "meta", // { institution_id, vehicle_id }
      granularity: "seconds",
    },
  });
  print("  [created - time series] gps_pings");
} else {
  print("  [exists] gps_pings");
}
ensureIndex("gps_pings", { "meta.institution_id": 1, "meta.vehicle_id": 1, ts: -1 }, { name: "idx_inst_vehicle_time" });
// =====================================================================
// MODULE 10 — FACILITIES MANAGEMENT
// =====================================================================
print("\n--- Facilities Management ---");

createOrUpdateCollection("assets", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    category: { bsonType: "string" },
    location: { bsonType: "string" },
    purchase_date: { bsonType: "date" },
  },
});
ensureIndex("assets", { institution_id: 1, category: 1 }, { name: "idx_inst_category" });

createOrUpdateCollection("work_orders", {
  bsonType: "object",
  required: ["institution_id", "reported_by", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    asset_id: { bsonType: "objectId" },
    reported_by: { bsonType: "objectId" },
    vendor_id: { bsonType: "objectId" },
    priority: { enum: ["low", "medium", "high", "critical"] },
    status: { enum: ["open", "assigned", "in_progress", "resolved", "closed"] },
    cost: { bsonType: "decimal" },
  },
});
ensureIndex("work_orders", { institution_id: 1, status: 1, priority: 1 }, { name: "idx_inst_status_priority" });
ensureIndex("work_orders", { institution_id: 1, asset_id: 1 }, { name: "idx_inst_asset" });

createOrUpdateCollection("inventory_items", {
  bsonType: "object",
  required: ["institution_id", "name", "sku", "quantity_on_hand"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    sku: { bsonType: "string" },
    quantity_on_hand: { bsonType: "int" },
    reorder_threshold: { bsonType: "int" },
  },
});
ensureIndex("inventory_items", { institution_id: 1, sku: 1 }, { unique: true, name: "uniq_inst_sku" });

createOrUpdateCollection("vendors", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    contact: { bsonType: "object" },
    category: { bsonType: "string" },
  },
});
ensureIndex("vendors", { institution_id: 1, name: 1 }, { name: "idx_inst_name" });

createOrUpdateCollection("utility_readings", {
  bsonType: "object",
  required: ["institution_id", "meter_id", "recorded_at"],
  properties: {
    institution_id: { bsonType: "objectId" },
    meter_id: { bsonType: "string" },
    reading_value: { bsonType: ["double", "int"] },
    recorded_at: { bsonType: "date" },
  },
});
ensureIndex("utility_readings", { institution_id: 1, meter_id: 1, recorded_at: -1 }, { name: "idx_inst_meter_time" });

createOrUpdateCollection("preventive_maintenance_schedules", {
  bsonType: "object",
  required: ["institution_id", "asset_id", "frequency"],
  properties: {
    institution_id: { bsonType: "objectId" },
    asset_id: { bsonType: "objectId" },
    frequency: { bsonType: "string" },
    next_due_date: { bsonType: "date" },
  },
});
ensureIndex("preventive_maintenance_schedules", { institution_id: 1, asset_id: 1 }, { name: "idx_inst_asset" });
// =====================================================================
// MODULE 11 — ALUMNI MANAGEMENT
// =====================================================================
print("\n--- Alumni Management ---");

createOrUpdateCollection("alumni_profiles", {
  bsonType: "object",
  required: ["institution_id", "user_id", "graduation_year"],
  properties: {
    institution_id: { bsonType: "objectId" },
    user_id: { bsonType: "objectId" },
    graduation_year: { bsonType: "int" },
    program_id: { bsonType: "objectId" },
    current_employer: { bsonType: "string" },
    contact_visibility: { enum: ["public", "alumni_only", "private"] },
  },
});
ensureIndex("alumni_profiles", { institution_id: 1, user_id: 1 }, { unique: true, name: "uniq_inst_user" });

createOrUpdateCollection("alumni_events", {
  bsonType: "object",
  required: ["institution_id", "name"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    event_date: { bsonType: "date" },
    rsvp_count: { bsonType: "int" },
  },
});
ensureIndex("alumni_events", { institution_id: 1, event_date: 1 }, { name: "idx_inst_date" });

createOrUpdateCollection("mentorship_pairs", {
  bsonType: "object",
  required: ["institution_id", "alumni_profile_id", "student_id", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    alumni_profile_id: { bsonType: "objectId" },
    student_id: { bsonType: "objectId" },
    status: { enum: ["active", "completed", "ended"] },
    paired_at: { bsonType: "date" },
  },
});
ensureIndex("mentorship_pairs", { institution_id: 1, alumni_profile_id: 1 }, { name: "idx_inst_alumni" });
ensureIndex("mentorship_pairs", { institution_id: 1, student_id: 1 }, { name: "idx_inst_student" });

createOrUpdateCollection("donations", {
  bsonType: "object",
  required: ["institution_id", "alumni_profile_id", "amount", "idempotency_key"],
  properties: {
    institution_id: { bsonType: "objectId" },
    alumni_profile_id: { bsonType: "objectId" },
    amount: { bsonType: "decimal" },
    campaign: { bsonType: "string" },
    payment_reference: { bsonType: "object" },
    idempotency_key: { bsonType: "string" },
    received_at: { bsonType: "date" },
  },
}, { validationAction: "error" }); // money — strict validation
ensureIndex("donations", { institution_id: 1, idempotency_key: 1 }, { unique: true, name: "uniq_inst_idempotency" });
ensureIndex("donations", { institution_id: 1, alumni_profile_id: 1 }, { name: "idx_inst_alumni" });

createOrUpdateCollection("engagement_scores", {
  bsonType: "object",
  required: ["institution_id", "alumni_profile_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    alumni_profile_id: { bsonType: "objectId" },
    score: { bsonType: ["double", "int"] },
    computed_at: { bsonType: "date" },
  },
});
ensureIndex("engagement_scores", { institution_id: 1, alumni_profile_id: 1 }, { unique: true, name: "uniq_inst_alumni" });
// =====================================================================
// MODULE 13 — COMMUNICATION & ANNOUNCEMENT
// =====================================================================
print("\n--- Communication & Announcement ---");

createOrUpdateCollection("announcements", {
  bsonType: "object",
  required: ["institution_id", "title"],
  properties: {
    institution_id: { bsonType: "objectId" },
    title: { bsonType: "string" },
    target_scope: { bsonType: "object" },
    published_at: { bsonType: "date" },
  },
});
ensureIndex("announcements", { institution_id: 1, published_at: -1 }, { name: "idx_inst_time" });

createOrUpdateCollection("campaigns", {
  bsonType: "object",
  required: ["institution_id", "name", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    name: { bsonType: "string" },
    steps: { bsonType: "array" }, // embedded bounded sequence — architecture doc §23.5
    target_segment: { bsonType: "object" },
    status: { enum: ["draft", "scheduled", "running", "completed", "paused"] },
    scheduled_start: { bsonType: "date" },
  },
});
ensureIndex("campaigns", { institution_id: 1, status: 1 }, { name: "idx_inst_status" });

createOrUpdateCollection("notification_logs", {
  bsonType: "object",
  required: ["institution_id", "recipient_id", "channel", "status"],
  properties: {
    institution_id: { bsonType: "objectId" },
    recipient_id: { bsonType: "objectId" },
    channel: { enum: ["push", "email", "sms", "whatsapp", "in_app"] },
    source_event: { bsonType: "string" },
    status: { enum: ["queued", "sent", "delivered", "failed"] },
    sent_at: { bsonType: "date" },
  },
}, { validationAction: "warn" }); // never block a notification write, same convention as audit_logs
ensureIndex("notification_logs", { institution_id: 1, recipient_id: 1, sent_at: -1 }, { name: "idx_inst_recipient_time" });
ensureIndex("notification_logs", { institution_id: 1, source_event: 1 }, { name: "idx_inst_sourceevent" });

createOrUpdateCollection("survey_responses", {
  bsonType: "object",
  required: ["institution_id", "survey_id", "respondent_id"],
  properties: {
    institution_id: { bsonType: "objectId" },
    survey_id: { bsonType: "objectId" },
    respondent_id: { bsonType: "objectId" },
    answers: { bsonType: "array" },
    submitted_at: { bsonType: "date" },
  },
});
ensureIndex("survey_responses", { institution_id: 1, survey_id: 1 }, { name: "idx_inst_survey" });

createOrUpdateCollection("emergency_alerts", {
  bsonType: "object",
  required: ["institution_id", "message", "severity"],
  properties: {
    institution_id: { bsonType: "objectId" },
    message: { bsonType: "string" },
    severity: { enum: ["advisory", "warning", "critical"] },
    target_scope: { bsonType: "object" },
    broadcast_at: { bsonType: "date" },
    acknowledgement_count: { bsonType: "int" },
  },
}, { validationAction: "error", validationLevel: "strict" }); // correctness prioritized over latency — architecture doc §23.3
ensureIndex("emergency_alerts", { institution_id: 1, broadcast_at: -1 }, { name: "idx_inst_time" });
// =====================================================================
// SHARDING (OPTIONAL — requires a sharded cluster tier)
// =====================================================================
// Atlas only supports sh.shardCollection() on Global Clusters or
// dedicated clusters with sharding explicitly enabled (M30+, configured
// as sharded). On Serverless / M0-M20 / unsharded dedicated clusters
// these calls will fail — that's expected, not a bug in this script.
//
// Set ENABLE_SHARDING = true below ONLY if you're on a sharded tier and
// have already defined shard zones (sh.addShardTag / sh.addTagRange)
// matching Section 10.2 of the architecture document. Left false by
// default so this script is always safe to run as a first pass.

const ENABLE_SHARDING = false;

if (ENABLE_SHARDING) {
  print("\n--- Sharding ---");
  try {
    sh.enableSharding(DB_NAME);

    const shardPlan = [
      { coll: "enrollments", key: { institution_id: 1, student_id: 1 } },
      { coll: "exam_results", key: { institution_id: 1, student_id: 1 } },
      { coll: "fee_invoices", key: { institution_id: 1, student_id: 1 } },
      { coll: "fee_receipts", key: { institution_id: 1, student_id: 1 } },
      { coll: "audit_logs", key: { institution_id: "hashed" } }, // hashed to avoid a time-ordered hotspot
      { coll: "users", key: { institution_id: 1, _id: "hashed" } },
      { coll: "submissions", key: { institution_id: 1, student_id: 1 } },
      { coll: "progress_tracking", key: { institution_id: 1, student_id: 1 } },
      { coll: "attendance_timesheets", key: { institution_id: 1, employee_id: 1 } },
      { coll: "payroll_runs", key: { institution_id: 1, employee_id: 1 } },
      { coll: "book_circulations", key: { institution_id: 1, member_id: 1 } },
      { coll: "hostel_checkins", key: { institution_id: 1, student_id: 1 } },
      { coll: "student_applications", key: { institution_id: 1, student_id: 1 } },
      { coll: "notification_logs", key: { institution_id: "hashed" } }, // hashed — same rationale as audit_logs
      // gps_pings is a time-series collection and shards automatically on
      // its metaField — it is intentionally NOT included in this list.
    ];

    shardPlan.forEach((p) => {
      const ns = DB_NAME + "." + p.coll;
      // Shard keys need a supporting index first.
      db.getCollection(p.coll).createIndex(p.key);
      sh.shardCollection(ns, p.key);
      print("  [sharded] " + ns + " on " + JSON.stringify(p.key));
    });
  } catch (e) {
    print("  [sharding skipped/failed] " + e.message);
    print("  This is expected on non-sharded Atlas tiers (Serverless, M0-M20, or unsharded dedicated clusters).");
  }
} else {
  print("\n--- Sharding ---");
  print("  Skipped (ENABLE_SHARDING = false). See Section 10 of the architecture");
  print("  document and the comment block above before enabling.");
}

// =====================================================================
// SUMMARY
// =====================================================================
print("\n=====================================================");
print(" Provisioning complete for database: " + DB_NAME);
print(" Total collections now present: " + db.getCollectionNames().length);
print("=====================================================");
print(" Next steps:");
print("  1. Atlas -> Database Access: create per-module service-account");
print("     users scoped to the collections they own (Section 13.2).");
print("  2. Atlas -> Search: build the Elasticsearch-equivalent Atlas");
print("     Search indexes for course_content, catalog_items,");
print("     question_banks, announcements, alumni_profiles.");
print("  3. If on a sharded tier, review shard zones (Section 10.2)");
print("     before setting ENABLE_SHARDING = true above.");
print("  4. All 13 modules now have full validators (v2.0). Revisit");
print("     field lists against real application code before go-live —");
print("     schemas here cover each collection's primary/most-referenced");
print("     fields, not every supplementary metadata field.");