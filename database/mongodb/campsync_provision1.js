/**
 * =====================================================================
 *  CampSync — MongoDB Atlas Provisioning Script
 *  Version: 1.0
 *  Companion to: CampSync_MongoDB_Architecture_v1.0.docx
 *
 *  WHAT THIS DOES
 *  - Creates every collection listed in the platform-wide collection map
 *  - Applies full $jsonSchema validators + indexes for the 4 detailed
 *    modules: Admin Manager, Academic Management, Examination
 *    Management, Finance & Accounts
 *  - Creates baseline collections + a tenant index for the other 9
 *    modules (Learning Management, HR, Library, Hostel, Placement,
 *    Transport, Facilities, Alumni, Communication) — these were only
 *    architecture-level in the source document, so validators here are
 *    intentionally light; tighten them once those modules get the same
 *    field-level design pass as the priority four.
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

const DB_NAME = "campsync"; // change per-tenant if using dedicated-DB tenancy
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
// REMAINING 9 MODULES — baseline collections only
// (architecture-level in the source document; tighten validators once
//  each module gets the same field-level design pass as the priority 4)
// =====================================================================
print("\n--- Remaining Modules (baseline) ---");

// Every baseline collection gets the same minimal shape: require
// institution_id, keep the validator loose (warn, not error) so these
// don't block writes while the real schema is still being finalized.
const BASELINE_MODULES = {
  // 3. Learning Management (LMS)
  course_content: {},
  assignments: {},
  submissions: {},
  discussion_threads: {},
  virtual_classrooms: {},
  progress_tracking: {},
  badges: {},

  // 5. Human Resource Management
  employees: {},
  onboarding_cases: {},
  leave_requests: {},
  attendance_timesheets: {},
  performance_reviews: {},
  training_records: {},
  payroll_runs: {},

  // 6. Library Management
  catalog_items: {},
  book_circulations: {},
  digital_library_assets: {},
  memberships: {},
  fines: {},

  // 7. Hostel Management
  rooms: {},
  room_allocations: {},
  hostel_checkins: {},
  mess_plans: {},
  visitor_logs: {},
  maintenance_requests: {},

  // 8. Placement Management
  companies: {},
  job_postings: {},
  placement_drives: {},
  student_applications: {},
  interview_schedules: {},
  offers: {},

  // 9. Transport Management (gps_pings created separately as time-series below)
  routes: {},
  vehicles: {},
  vehicle_assignments: {},
  driver_profiles: {},

  // 10. Facilities Management
  assets: {},
  work_orders: {},
  inventory_items: {},
  vendors: {},
  utility_readings: {},
  preventive_maintenance_schedules: {},

  // 11. Alumni Management
  alumni_profiles: {},
  alumni_events: {},
  mentorship_pairs: {},
  donations: {},
  engagement_scores: {},

  // 13. Communication & Announcement
  announcements: {},
  campaigns: {},
  notification_logs: {},
  survey_responses: {},
  emergency_alerts: {},
};

Object.keys(BASELINE_MODULES).forEach((name) => {
  createOrUpdateCollection(
    name,
    {
      bsonType: "object",
      required: ["institution_id"],
      properties: { institution_id: { bsonType: "objectId" } },
    },
    { validationAction: "warn" }
  );
  ensureIndex(name, { institution_id: 1 }, { name: "idx_inst" });
});

// gps_pings is high-frequency telemetry — use a time-series collection
// (Transport Management module) instead of the generic baseline shape.
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
print("  4. Tighten the 'warn'-level baseline validators for the 9");
print("     remaining modules once they get a full schema design pass.");