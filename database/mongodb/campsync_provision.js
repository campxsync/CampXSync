/**
 * ============================================================================
 * CAMPSYNC CORE PLATFORM - MONGODB PROVISIONING & SCHEMA SCRIPT
 * Document Version: 2.0 (Covering all 13 Core Modules)
 * Compatible with: MongoDB 7.x & mongosh
 * 
 * Usage:
 *   mongosh "mongodb://localhost:27017/campsync" campsync_provision.js
 *   OR inside mongosh: load("campsync_provision.js")
 * ============================================================================
 */

// Switch to Target Database
db = db.getSiblingDB('campsync');

print("============================================================================");
print(" Starting CampSync MongoDB Architecture v2.0 Provisioning Script...");
print(" Target Database: " + db.getName());
print("============================================================================");

/**
 * Utility Helper to Create Collections with Validators Safely (Idempotent)
 */
function createCollectionSafely(collectionName, options) {
    options = options || {};
    var exists = db.getCollectionNames().indexOf(collectionName) !== -1;
    if (!exists) {
        try {
            db.createCollection(collectionName, options);
            print("  [+] Created collection: " + collectionName);
        } catch (e) {
            print("  [!] Failed to create collection " + collectionName + ": " + e.message);
        }
    } else if (options.validator) {
        try {
            db.runCommand({
                collMod: collectionName,
                validator: options.validator,
                validationLevel: options.validationLevel || "strict",
                validationAction: options.validationAction || "error"
            });
            print("  [*] Updated validator for existing collection: " + collectionName);
        } catch (e) {
            print("  [!] Failed to update validator for " + collectionName + ": " + e.message);
        }
    } else {
        print("  [-] Collection already exists: " + collectionName);
    }
}

/**
 * Utility Helper to Create Indexes Safely
 */
function createIndexSafely(collectionName, indexKeys, options) {
    options = options || {};
    try {
        db.getCollection(collectionName).createIndex(indexKeys, options);
        var idxName = options.name || JSON.stringify(indexKeys);
        print("    └─ Index created on " + collectionName + ": " + idxName);
    } catch (e) {
        print("    └─ [!] Index creation failed on " + collectionName + ": " + e.message);
    }
}


// ============================================================================
// MODULE 1: ACADEMIC MANAGEMENT
// ============================================================================
print("\n--- [Module 1/13] Provisioning Academic Management ---");

createCollectionSafely("programs", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "code", "name", "degree_level", "duration_years"],
            properties: {
                institution_id: { bsonType: "objectId", description: "Tenant identifier" },
                code: { bsonType: "string" },
                name: { bsonType: "string" },
                degree_level: { enum: ["diploma", "bachelor", "master", "doctorate"] },
                duration_years: { bsonType: "int" }
            }
        }
    }
});
createIndexSafely("programs", { institution_id: 1, code: 1 }, { unique: true });

createCollectionSafely("courses", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "course_code", "title", "credits"],
            properties: {
                institution_id: { bsonType: "objectId" },
                course_code: { bsonType: "string" },
                title: { bsonType: "string" },
                credits: { bsonType: ["int", "double"] }
            }
        }
    }
});
createIndexSafely("courses", { institution_id: 1, course_code: 1 }, { unique: true });

createCollectionSafely("course_sections", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "course_id", "term_id", "faculty_id", "capacity", "seats_filled", "status"],
            properties: {
                institution_id: { bsonType: "objectId" },
                course_id: { bsonType: "objectId" },
                term_id: { bsonType: "objectId" },
                faculty_id: { bsonType: "objectId" },
                capacity: { bsonType: "int" },
                seats_filled: { bsonType: "int" },
                status: { enum: ["draft", "open", "closed", "archived"] }
            }
        }
    }
});
createIndexSafely("course_sections", { institution_id: 1, term_id: 1, faculty_id: 1 });
createIndexSafely("course_sections", { institution_id: 1, course_id: 1, term_id: 1 }, { unique: true });

createCollectionSafely("class_schedules");
createIndexSafely("class_schedules", { institution_id: 1, room_id: 1, day_of_week: 1, start_time: 1 });

createCollectionSafely("enrollments", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "student_id", "course_section_id", "term_id", "status"],
            properties: {
                institution_id: { bsonType: "objectId" },
                student_id: { bsonType: "objectId" },
                course_section_id: { bsonType: "objectId" },
                term_id: { bsonType: "objectId" },
                status: { enum: ["pending_fee", "active", "dropped", "completed"] },
                fee_invoice_id: { bsonType: ["objectId", "null"] }
            }
        }
    }
});
createIndexSafely("enrollments", { institution_id: 1, student_id: 1, term_id: 1 }, { unique: true });
createIndexSafely("enrollments", { institution_id: 1, course_section_id: 1, status: 1 });

createCollectionSafely("academic_calendars");
createIndexSafely("academic_calendars", { institution_id: 1, academic_year: 1, term_name: 1 }, { unique: true });

createCollectionSafely("learning_pathways");
createIndexSafely("learning_pathways", { institution_id: 1, student_id: 1 });


// ============================================================================
// MODULE 2: EXAMINATION MANAGEMENT
// ============================================================================
print("\n--- [Module 2/13] Provisioning Examination Management ---");

createCollectionSafely("exam_registrations");
createIndexSafely("exam_registrations", { institution_id: 1, student_id: 1, exam_timetable_id: 1 }, { unique: true });

createCollectionSafely("exam_timetables");
createIndexSafely("exam_timetables", { institution_id: 1, term_id: 1, exam_date: 1 });

createCollectionSafely("hall_tickets");
createIndexSafely("hall_tickets", { institution_id: 1, exam_registration_id: 1 }, { unique: true });

createCollectionSafely("question_banks");
createIndexSafely("question_banks", { institution_id: 1, course_id: 1, difficulty_level: 1 });

createCollectionSafely("exam_results", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "exam_registration_id", "student_id", "course_id", "status"],
            properties: {
                institution_id: { bsonType: "objectId" },
                exam_registration_id: { bsonType: "objectId" },
                student_id: { bsonType: "objectId" },
                course_id: { bsonType: "objectId" },
                total_obtained: { bsonType: ["double", "int", "number"] },
                total_max: { bsonType: ["double", "int", "number"] },
                status: { enum: ["draft", "moderation", "published", "under_revaluation", "finalized", "superseded"] },
                revision_of: { bsonType: ["objectId", "null"] }
            }
        }
    }
});
createIndexSafely("exam_results", { institution_id: 1, student_id: 1, course_id: 1, status: 1 });
createIndexSafely("exam_results", { institution_id: 1, exam_registration_id: 1 }, { unique: true });

createCollectionSafely("revaluation_requests");
createIndexSafely("revaluation_requests", { institution_id: 1, exam_result_id: 1 });

createCollectionSafely("certificates");
createIndexSafely("certificates", { institution_id: 1, student_id: 1, certificate_type: 1 });


// ============================================================================
// MODULE 3: LEARNING MANAGEMENT (LMS)
// ============================================================================
print("\n--- [Module 3/13] Provisioning Learning Management (LMS) ---");

createCollectionSafely("course_content");
createIndexSafely("course_content", { institution_id: 1, course_section_id: 1, module_order: 1 });

createCollectionSafely("assignments");
createIndexSafely("assignments", { institution_id: 1, course_section_id: 1, due_date: 1 });

createCollectionSafely("submissions", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "assignment_id", "student_id", "submitted_at", "status"],
            properties: {
                institution_id: { bsonType: "objectId" },
                assignment_id: { bsonType: "objectId" },
                student_id: { bsonType: "objectId" },
                attempt_number: { bsonType: "int" },
                status: { enum: ["submitted", "late", "graded", "resubmission_requested"] }
            }
        }
    }
});
createIndexSafely("submissions", { institution_id: 1, assignment_id: 1, student_id: 1, attempt_number: 1 }, { unique: true });

createCollectionSafely("discussion_threads");
createIndexSafely("discussion_threads", { institution_id: 1, course_section_id: 1, parent_id: 1 });

createCollectionSafely("virtual_classrooms");
createIndexSafely("virtual_classrooms", { institution_id: 1, course_section_id: 1, scheduled_start: 1 });

createCollectionSafely("progress_tracking");
createIndexSafely("progress_tracking", { institution_id: 1, student_id: 1, course_section_id: 1 }, { unique: true });

createCollectionSafely("badges");
createIndexSafely("badges", { institution_id: 1, student_id: 1, badge_code: 1 });


// ============================================================================
// MODULE 4: FINANCE & ACCOUNTS
// ============================================================================
print("\n--- [Module 4/13] Provisioning Finance & Accounts ---");

createCollectionSafely("fee_structures");
createIndexSafely("fee_structures", { institution_id: 1, program_id: 1, academic_year: 1 });

createCollectionSafely("fee_invoices", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "student_id", "fee_structure_id", "total_amount", "amount_paid", "status", "due_date"],
            properties: {
                institution_id: { bsonType: "objectId" },
                student_id: { bsonType: "objectId" },
                total_amount: { bsonType: "decimal" },
                amount_paid: { bsonType: "decimal" },
                balance: { bsonType: "decimal" },
                status: { enum: ["issued", "partially_paid", "paid", "overdue", "waived", "cancelled"] },
                version: { bsonType: "int" }
            }
        }
    }
});
createIndexSafely("fee_invoices", { institution_id: 1, student_id: 1, status: 1, due_date: 1 });
createIndexSafely("fee_invoices", { institution_id: 1, enrollment_id: 1 });

createCollectionSafely("fee_receipts", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "fee_invoice_id", "amount", "payment_method", "receipt_number", "idempotency_key"],
            properties: {
                institution_id: { bsonType: "objectId" },
                fee_invoice_id: { bsonType: "objectId" },
                amount: { bsonType: "decimal" },
                payment_method: { enum: ["card", "upi", "netbanking", "dd", "cash"] },
                receipt_number: { bsonType: "string" },
                idempotency_key: { bsonType: "string" }
            }
        }
    }
});
createIndexSafely("fee_receipts", { institution_id: 1, idempotency_key: 1 }, { unique: true });
createIndexSafely("fee_receipts", { institution_id: 1, fee_invoice_id: 1 });

createCollectionSafely("scholarships");
createIndexSafely("scholarships", { institution_id: 1, student_id: 1 });

createCollectionSafely("expenses");
createIndexSafely("expenses", { institution_id: 1, category: 1, incurred_date: 1 });

createCollectionSafely("budgets");
createIndexSafely("budgets", { institution_id: 1, department_id: 1, financial_year: 1 }, { unique: true });

createCollectionSafely("financial_reports");


// ============================================================================
// MODULE 5: HUMAN RESOURCE MANAGEMENT
// ============================================================================
print("\n--- [Module 5/13] Provisioning Human Resource Management ---");

createCollectionSafely("employees", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "user_id", "employee_code", "department_id", "designation", "status"],
            properties: {
                institution_id: { bsonType: "objectId" },
                user_id: { bsonType: "objectId" },
                employee_code: { bsonType: "string" },
                status: { enum: ["active", "on_leave", "separated"] }
            }
        }
    }
});
createIndexSafely("employees", { institution_id: 1, employee_code: 1 }, { unique: true });
createIndexSafely("employees", { institution_id: 1, user_id: 1 }, { unique: true });
createIndexSafely("employees", { institution_id: 1, department_id: 1 });

createCollectionSafely("onboarding_cases");
createCollectionSafely("leave_requests");
createIndexSafely("leave_requests", { institution_id: 1, employee_id: 1, status: 1 });

createCollectionSafely("attendance_timesheets");
createIndexSafely("attendance_timesheets", { institution_id: 1, employee_id: 1, date: 1 }, { unique: true });

createCollectionSafely("performance_reviews");
createCollectionSafely("training_records");

createCollectionSafely("payroll_runs", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "employee_id", "period", "gross_amount", "net_amount", "status", "idempotency_key"],
            properties: {
                institution_id: { bsonType: "objectId" },
                employee_id: { bsonType: "objectId" },
                period: { bsonType: "string" },
                gross_amount: { bsonType: "decimal" },
                net_amount: { bsonType: "decimal" },
                status: { enum: ["draft", "approved", "disbursed", "reversed"] },
                idempotency_key: { bsonType: "string" }
            }
        }
    }
});
createIndexSafely("payroll_runs", { institution_id: 1, idempotency_key: 1 }, { unique: true });
createIndexSafely("payroll_runs", { institution_id: 1, employee_id: 1, period: 1 });


// ============================================================================
// MODULE 6: LIBRARY MANAGEMENT
// ============================================================================
print("\n--- [Module 6/13] Provisioning Library Management ---");

createCollectionSafely("catalog_items");
createIndexSafely("catalog_items", { institution_id: 1, title: 1, isbn: 1 });

createCollectionSafely("book_circulations");
createIndexSafely("book_circulations", { institution_id: 1, member_id: 1, status: 1 });
createIndexSafely("book_circulations", { institution_id: 1, catalog_item_id: 1, status: 1 });

createCollectionSafely("digital_library_assets");
createCollectionSafely("memberships");
createIndexSafely("memberships", { institution_id: 1, user_id: 1 }, { unique: true });

createCollectionSafely("fines");
createIndexSafely("fines", { institution_id: 1, member_id: 1, status: 1 });


// ============================================================================
// MODULE 7: HOSTEL MANAGEMENT
// ============================================================================
print("\n--- [Module 7/13] Provisioning Hostel Management ---");

createCollectionSafely("rooms");
createIndexSafely("rooms", { institution_id: 1, hostel_name: 1, room_number: 1 }, { unique: true });

createCollectionSafely("room_allocations");
createIndexSafely("room_allocations", { institution_id: 1, student_id: 1, academic_year: 1, status: 1 });

createCollectionSafely("hostel_checkins");
createIndexSafely("hostel_checkins", { institution_id: 1, student_id: 1, recorded_at: -1 });

createCollectionSafely("mess_plans");
createIndexSafely("mess_plans", { institution_id: 1, student_id: 1, term_id: 1 });

createCollectionSafely("visitor_logs");
createCollectionSafely("maintenance_requests");
createIndexSafely("maintenance_requests", { institution_id: 1, room_id: 1, status: 1 });


// ============================================================================
// MODULE 8: PLACEMENT MANAGEMENT
// ============================================================================
print("\n--- [Module 8/13] Provisioning Placement Management ---");

createCollectionSafely("companies");
createIndexSafely("companies", { institution_id: 1, name: 1 }, { unique: true });

createCollectionSafely("job_postings");
createIndexSafely("job_postings", { institution_id: 1, company_id: 1, status: 1 });

createCollectionSafely("placement_drives");
createCollectionSafely("student_applications", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "student_id", "job_posting_id", "status"],
            properties: {
                institution_id: { bsonType: "objectId" },
                student_id: { bsonType: "objectId" },
                job_posting_id: { bsonType: "objectId" },
                status: { enum: ["applied", "shortlisted", "interviewing", "offered", "rejected", "withdrawn"] }
            }
        }
    }
});
createIndexSafely("student_applications", { institution_id: 1, student_id: 1, job_posting_id: 1 }, { unique: true });

createCollectionSafely("interview_schedules");
createCollectionSafely("offers");
createIndexSafely("offers", { institution_id: 1, student_application_id: 1 }, { unique: true });


// ============================================================================
// MODULE 9: TRANSPORT MANAGEMENT (Includes Time-Series Collection)
// ============================================================================
print("\n--- [Module 9/13] Provisioning Transport Management ---");

createCollectionSafely("routes");
createIndexSafely("routes", { institution_id: 1, route_code: 1 }, { unique: true });

createCollectionSafely("vehicles");
createIndexSafely("vehicles", { institution_id: 1, registration_number: 1 }, { unique: true });

createCollectionSafely("vehicle_assignments");
createIndexSafely("vehicle_assignments", { institution_id: 1, vehicle_id: 1, route_id: 1, driver_id: 1 });

createCollectionSafely("driver_profiles");

// Special Native Time-Series Collection for High-Frequency GPS Pings
createCollectionSafely("gps_pings", {
    timeseries: {
        timeField: "ts",
        metaField: "meta",
        granularity: "seconds"
    },
    expireAfterSeconds: 7776000 // 90 days automatic TTL eviction
});
createIndexSafely("gps_pings", { "meta.institution_id": 1, "meta.vehicle_id": 1, "ts": -1 });


// ============================================================================
// MODULE 10: FACILITIES MANAGEMENT
// ============================================================================
print("\n--- [Module 10/13] Provisioning Facilities Management ---");

createCollectionSafely("assets");
createIndexSafely("assets", { institution_id: 1, asset_tag: 1 }, { unique: true });

createCollectionSafely("work_orders");
createIndexSafely("work_orders", { institution_id: 1, asset_id: 1, status: 1 });

createCollectionSafely("inventory_items");
createIndexSafely("inventory_items", { institution_id: 1, sku: 1 }, { unique: true });

createCollectionSafely("vendors");
createCollectionSafely("utility_readings");
createCollectionSafely("preventive_maintenance_schedules");


// ============================================================================
// MODULE 11: ALUMNI MANAGEMENT
// ============================================================================
print("\n--- [Module 11/13] Provisioning Alumni Management ---");

createCollectionSafely("alumni_profiles");
createIndexSafely("alumni_profiles", { institution_id: 1, user_id: 1 }, { unique: true });

createCollectionSafely("alumni_events");
createCollectionSafely("mentorship_pairs");

createCollectionSafely("donations");
createIndexSafely("donations", { institution_id: 1, idempotency_key: 1 }, { unique: true });
createIndexSafely("donations", { institution_id: 1, alumni_profile_id: 1 });

createCollectionSafely("engagement_scores");


// ============================================================================
// MODULE 12: ADMIN MANAGER (Identity, RBAC & Audit Ledger)
// ============================================================================
print("\n--- [Module 12/13] Provisioning Admin Manager ---");

createCollectionSafely("users", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["institution_id", "auth", "profile_type", "status"],
            properties: {
                institution_id: { bsonType: "objectId" },
                auth: {
                    bsonType: "object",
                    required: ["email"],
                    properties: {
                        email: { bsonType: "string" },
                        password_hash: { bsonType: "string" }
                    }
                },
                profile_type: { enum: ["student", "faculty", "staff", "parent", "alumni", "admin"] },
                status: { enum: ["active", "suspended", "deactivated"] }
            }
        }
    }
});
createIndexSafely("users", { institution_id: 1, "auth.email": 1 }, { unique: true });
createIndexSafely("users", { institution_id: 1, profile_type: 1, status: 1 });

createCollectionSafely("roles");
createIndexSafely("roles", { institution_id: 1, code: 1 }, { unique: true });

createCollectionSafely("permissions");
createIndexSafely("permissions", { resource: 1, action: 1 }, { unique: true });

createCollectionSafely("role_assignments");
createIndexSafely("role_assignments", { institution_id: 1, user_id: 1 });
createIndexSafely("role_assignments", { institution_id: 1, role_id: 1, "scope.scope_id": 1 });

createCollectionSafely("system_configs");
createIndexSafely("system_configs", { institution_id: 1, config_key: 1 }, { unique: true });

// Audit Logs (Immutable, Append-Only platform event ledger)
createCollectionSafely("audit_logs");
createIndexSafely("audit_logs", { institution_id: 1, occurred_at: -1 });
createIndexSafely("audit_logs", { institution_id: 1, "target.document_id": 1 });
createIndexSafely("audit_logs", { institution_id: 1, event_type: 1, occurred_at: -1 });

// Outbox Pattern events table for reliable Kafka change-stream relay
createCollectionSafely("outbox_events");
createIndexSafely("outbox_events", { published: 1, created_at: 1 });


// ============================================================================
// MODULE 13: COMMUNICATION & ANNOUNCEMENT
// ============================================================================
print("\n--- [Module 13/13] Provisioning Communication & Announcement ---");

createCollectionSafely("announcements");
createIndexSafely("announcements", { institution_id: 1, published_at: -1 });

createCollectionSafely("campaigns");
createIndexSafely("campaigns", { institution_id: 1, status: 1 });

createCollectionSafely("notification_logs");
createIndexSafely("notification_logs", { institution_id: 1, recipient_id: 1, sent_at: -1 });

createCollectionSafely("survey_responses");
createCollectionSafely("emergency_alerts");
createIndexSafely("emergency_alerts", { institution_id: 1, broadcast_at: -1 });


// ============================================================================
// SEED INITIAL PLATFORM DATA
// ============================================================================
print("\n--- [Seed] Inserting Default Platform Seed Data ---");

// 1. Default System Institution
var sampleInstitutionId = new ObjectId("650000000000000000000001");

// 2. Default System Roles
var rolesToSeed = [
    { _id: new ObjectId("650000000000000000000101"), institution_id: sampleInstitutionId, code: "SUPER_ADMIN", name: "Super Administrator", is_system: true },
    { _id: new ObjectId("650000000000000000000102"), institution_id: sampleInstitutionId, code: "INSTITUTION_ADMIN", name: "Institution Administrator", is_system: true },
    { _id: new ObjectId("650000000000000000000103"), institution_id: sampleInstitutionId, code: "FACULTY", name: "Faculty Member", is_system: true },
    { _id: new ObjectId("650000000000000000000104"), institution_id: sampleInstitutionId, code: "STUDENT", name: "Student", is_system: true }
];

for (var i = 0; i < rolesToSeed.length; i++) {
    var r = rolesToSeed[i];
    try {
        db.roles.updateOne({ _id: r._id }, { $setOnInsert: r }, { upsert: true });
    } catch (e) {
        print("  [!] Role seed warning: " + e.message);
    }
}
print("  [+] System roles initialized.");

// 3. System Config Entry
try {
    db.system_configs.updateOne(
        { institution_id: sampleInstitutionId, config_key: "platform_version" },
        { $set: { config_value: "2.0.0", updated_at: new Date() } },
        { upsert: true }
    );
    print("  [+] Platform system config seed applied.");
} catch (e) {
    print("  [!] System config seed warning: " + e.message);
}

print("\n============================================================================");
print(" CampSync MongoDB Architecture v2.0 Provisioning Completed Successfully!");
print(" Total Collections Checked/Created: " + db.getCollectionNames().length);
print("============================================================================");
