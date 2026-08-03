"""
============================================================================
CAMPSYNC CORE PLATFORM - MONGO DB ATLAS PROVISIONER
Document Version: 2.0 (Covering all 13 Core Modules)

Usage:
  python run_atlas_provision.py "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/campsync?retryWrites=true&w=majority"
============================================================================
"""

import sys
import os
import pymongo
from bson.objectid import ObjectId
from datetime import datetime

def main():
    if len(sys.argv) > 1:
        uri = sys.argv[1]
    else:
        uri = os.environ.get("MONGODB_ATLAS_URI")

    if not uri or "<username>" in uri or "<password>" in uri:
        print("============================================================================")
        print("[!] ERROR: Please provide your MongoDB Atlas Connection String.")
        print("============================================================================")
        print("Usage:")
        print("  python run_atlas_provision.py \"mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/campsync?retryWrites=true&w=majority\"")
        print("Or set environment variable:")
        print("  $env:MONGODB_ATLAS_URI=\"mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/campsync\"")
        sys.exit(1)

    print("============================================================================")
    print(" Connecting to MongoDB Atlas...")
    print("============================================================================")

    try:
        client = pymongo.MongoClient(uri, serverSelectionTimeoutMS=10000)
        # Test connection
        client.admin.command('ping')
        print("[+] Successfully connected to MongoDB Atlas!")
    except Exception as e:
        print(f"[!] Connection Failed: {e}")
        sys.exit(1)

    db = client.get_database("campsync")
    print(f"[*] Provisioning database: '{db.name}'")

    existing_colls = db.list_collection_names()

    def create_coll(name, validator=None, timeseries=None, expire_seconds=None):
        if name not in existing_colls:
            kwargs = {}
            if validator:
                kwargs['validator'] = validator
            if timeseries:
                kwargs['timeseries'] = timeseries
            if expire_seconds:
                kwargs['expireAfterSeconds'] = expire_seconds
            try:
                db.create_collection(name, **kwargs)
                print(f"  [+] Created collection: {name}")
            except Exception as ex:
                print(f"  [!] Collection {name} warning: {ex}")
        else:
            print(f"  [-] Collection already exists: {name}")

    def create_idx(coll_name, index_keys, unique=False, name=None):
        try:
            coll = db[coll_name]
            kwargs = {}
            if unique:
                kwargs['unique'] = True
            if name:
                kwargs['name'] = name
            coll.create_index(index_keys, **kwargs)
            print(f"    -> Index created on {coll_name}: {index_keys}")
        except Exception as ex:
            print(f"    -> [!] Index creation warning on {coll_name}: {ex}")

    # =========================================================================
    # MODULE 1: ACADEMIC MANAGEMENT
    # =========================================================================
    print("\n--- [Module 1/13] Academic Management ---")
    create_coll("programs")
    create_idx("programs", [("institution_id", 1), ("code", 1)], unique=True)

    create_coll("courses")
    create_idx("courses", [("institution_id", 1), ("course_code", 1)], unique=True)

    create_coll("course_sections")
    create_idx("course_sections", [("institution_id", 1), ("term_id", 1), ("faculty_id", 1)])
    create_idx("course_sections", [("institution_id", 1), ("course_id", 1), ("term_id", 1)], unique=True)

    create_coll("class_schedules")
    create_idx("class_schedules", [("institution_id", 1), ("room_id", 1), ("day_of_week", 1), ("start_time", 1)])

    create_coll("enrollments")
    create_idx("enrollments", [("institution_id", 1), ("student_id", 1), ("term_id", 1)], unique=True)
    create_idx("enrollments", [("institution_id", 1), ("course_section_id", 1), ("status", 1)])

    create_coll("academic_calendars")
    create_idx("academic_calendars", [("institution_id", 1), ("academic_year", 1), ("term_name", 1)], unique=True)

    create_coll("learning_pathways")
    create_idx("learning_pathways", [("institution_id", 1), ("student_id", 1)])

    # =========================================================================
    # MODULE 2: EXAMINATION MANAGEMENT
    # =========================================================================
    print("\n--- [Module 2/13] Examination Management ---")
    create_coll("exam_registrations")
    create_idx("exam_registrations", [("institution_id", 1), ("student_id", 1), ("exam_timetable_id", 1)], unique=True)

    create_coll("exam_timetables")
    create_idx("exam_timetables", [("institution_id", 1), ("term_id", 1), ("exam_date", 1)])

    create_coll("hall_tickets")
    create_idx("hall_tickets", [("institution_id", 1), ("exam_registration_id", 1)], unique=True)

    create_coll("question_banks")
    create_idx("question_banks", [("institution_id", 1), ("course_id", 1), ("difficulty_level", 1)])

    create_coll("exam_results")
    create_idx("exam_results", [("institution_id", 1), ("student_id", 1), ("course_id", 1), ("status", 1)])
    create_idx("exam_results", [("institution_id", 1), ("exam_registration_id", 1)], unique=True)

    create_coll("revaluation_requests")
    create_idx("revaluation_requests", [("institution_id", 1), ("exam_result_id", 1)])

    create_coll("certificates")
    create_idx("certificates", [("institution_id", 1), ("student_id", 1), ("certificate_type", 1)])

    # =========================================================================
    # MODULE 3: LEARNING MANAGEMENT (LMS)
    # =========================================================================
    print("\n--- [Module 3/13] Learning Management (LMS) ---")
    create_coll("course_content")
    create_idx("course_content", [("institution_id", 1), ("course_section_id", 1), ("module_order", 1)])

    create_coll("assignments")
    create_idx("assignments", [("institution_id", 1), ("course_section_id", 1), ("due_date", 1)])

    create_coll("submissions")
    create_idx("submissions", [("institution_id", 1), ("assignment_id", 1), ("student_id", 1), ("attempt_number", 1)], unique=True)

    create_coll("discussion_threads")
    create_idx("discussion_threads", [("institution_id", 1), ("course_section_id", 1), ("parent_id", 1)])

    create_coll("virtual_classrooms")
    create_idx("virtual_classrooms", [("institution_id", 1), ("course_section_id", 1), ("scheduled_start", 1)])

    create_coll("progress_tracking")
    create_idx("progress_tracking", [("institution_id", 1), ("student_id", 1), ("course_section_id", 1)], unique=True)

    create_coll("badges")
    create_idx("badges", [("institution_id", 1), ("student_id", 1), ("badge_code", 1)])

    # =========================================================================
    # MODULE 4: FINANCE & ACCOUNTS
    # =========================================================================
    print("\n--- [Module 4/13] Finance & Accounts ---")
    create_coll("fee_structures")
    create_idx("fee_structures", [("institution_id", 1), ("program_id", 1), ("academic_year", 1)])

    create_coll("fee_invoices")
    create_idx("fee_invoices", [("institution_id", 1), ("student_id", 1), ("status", 1), ("due_date", 1)])
    create_idx("fee_invoices", [("institution_id", 1), ("enrollment_id", 1)])

    create_coll("fee_receipts")
    create_idx("fee_receipts", [("institution_id", 1), ("idempotency_key", 1)], unique=True)
    create_idx("fee_receipts", [("institution_id", 1), ("fee_invoice_id", 1)])

    create_coll("scholarships")
    create_idx("scholarships", [("institution_id", 1), ("student_id", 1)])

    create_coll("expenses")
    create_idx("expenses", [("institution_id", 1), ("category", 1), ("incurred_date", 1)])

    create_coll("budgets")
    create_idx("budgets", [("institution_id", 1), ("department_id", 1), ("financial_year", 1)], unique=True)

    create_coll("financial_reports")

    # =========================================================================
    # MODULE 5: HUMAN RESOURCE MANAGEMENT
    # =========================================================================
    print("\n--- [Module 5/13] Human Resource Management ---")
    create_coll("employees")
    create_idx("employees", [("institution_id", 1), ("employee_code", 1)], unique=True)
    create_idx("employees", [("institution_id", 1), ("user_id", 1)], unique=True)
    create_idx("employees", [("institution_id", 1), ("department_id", 1)])

    create_coll("onboarding_cases")
    create_coll("leave_requests")
    create_idx("leave_requests", [("institution_id", 1), ("employee_id", 1), ("status", 1)])

    create_coll("attendance_timesheets")
    create_idx("attendance_timesheets", [("institution_id", 1), ("employee_id", 1), ("date", 1)], unique=True)

    create_coll("performance_reviews")
    create_coll("training_records")

    create_coll("payroll_runs")
    create_idx("payroll_runs", [("institution_id", 1), ("idempotency_key", 1)], unique=True)
    create_idx("payroll_runs", [("institution_id", 1), ("employee_id", 1), ("period", 1)])

    # =========================================================================
    # MODULE 6: LIBRARY MANAGEMENT
    # =========================================================================
    print("\n--- [Module 6/13] Library Management ---")
    create_coll("catalog_items")
    create_idx("catalog_items", [("institution_id", 1), ("title", 1), ("isbn", 1)])

    create_coll("book_circulations")
    create_idx("book_circulations", [("institution_id", 1), ("member_id", 1), ("status", 1)])
    create_idx("book_circulations", [("institution_id", 1), ("catalog_item_id", 1), ("status", 1)])

    create_coll("digital_library_assets")
    create_coll("memberships")
    create_idx("memberships", [("institution_id", 1), ("user_id", 1)], unique=True)

    create_coll("fines")
    create_idx("fines", [("institution_id", 1), ("member_id", 1), ("status", 1)])

    # =========================================================================
    # MODULE 7: HOSTEL MANAGEMENT
    # =========================================================================
    print("\n--- [Module 7/13] Hostel Management ---")
    create_coll("rooms")
    create_idx("rooms", [("institution_id", 1), ("hostel_name", 1), ("room_number", 1)], unique=True)

    create_coll("room_allocations")
    create_idx("room_allocations", [("institution_id", 1), ("student_id", 1), ("academic_year", 1), ("status", 1)])

    create_coll("hostel_checkins")
    create_idx("hostel_checkins", [("institution_id", 1), ("student_id", 1), ("recorded_at", -1)])

    create_coll("mess_plans")
    create_idx("mess_plans", [("institution_id", 1), ("student_id", 1), ("term_id", 1)])

    create_coll("visitor_logs")
    create_coll("maintenance_requests")
    create_idx("maintenance_requests", [("institution_id", 1), ("room_id", 1), ("status", 1)])

    # =========================================================================
    # MODULE 8: PLACEMENT MANAGEMENT
    # =========================================================================
    print("\n--- [Module 8/13] Placement Management ---")
    create_coll("companies")
    create_idx("companies", [("institution_id", 1), ("name", 1)], unique=True)

    create_coll("job_postings")
    create_idx("job_postings", [("institution_id", 1), ("company_id", 1), ("status", 1)])

    create_coll("placement_drives")
    create_coll("student_applications")
    create_idx("student_applications", [("institution_id", 1), ("student_id", 1), ("job_posting_id", 1)], unique=True)

    create_coll("interview_schedules")
    create_coll("offers")
    create_idx("offers", [("institution_id", 1), ("student_application_id", 1)], unique=True)

    # =========================================================================
    # MODULE 9: TRANSPORT MANAGEMENT (Time-Series)
    # =========================================================================
    print("\n--- [Module 9/13] Transport Management ---")
    create_coll("routes")
    create_idx("routes", [("institution_id", 1), ("route_code", 1)], unique=True)

    create_coll("vehicles")
    create_idx("vehicles", [("institution_id", 1), ("registration_number", 1)], unique=True)

    create_coll("vehicle_assignments")
    create_idx("vehicle_assignments", [("institution_id", 1), ("vehicle_id", 1), ("route_id", 1), ("driver_id", 1)])

    create_coll("driver_profiles")

    # Native Time-Series Collection
    ts_config = {"timeField": "ts", "metaField": "meta", "granularity": "seconds"}
    create_coll("gps_pings", timeseries=ts_config, expire_seconds=7776000)
    create_idx("gps_pings", [("meta.institution_id", 1), ("meta.vehicle_id", 1), ("ts", -1)])

    # =========================================================================
    # MODULE 10: FACILITIES MANAGEMENT
    # =========================================================================
    print("\n--- [Module 10/13] Facilities Management ---")
    create_coll("assets")
    create_idx("assets", [("institution_id", 1), ("asset_tag", 1)], unique=True)

    create_coll("work_orders")
    create_idx("work_orders", [("institution_id", 1), ("asset_id", 1), ("status", 1)])

    create_coll("inventory_items")
    create_idx("inventory_items", [("institution_id", 1), ("sku", 1)], unique=True)

    create_coll("vendors")
    create_coll("utility_readings")
    create_coll("preventive_maintenance_schedules")

    # =========================================================================
    # MODULE 11: ALUMNI MANAGEMENT
    # =========================================================================
    print("\n--- [Module 11/13] Alumni Management ---")
    create_coll("alumni_profiles")
    create_idx("alumni_profiles", [("institution_id", 1), ("user_id", 1)], unique=True)

    create_coll("alumni_events")
    create_coll("mentorship_pairs")

    create_coll("donations")
    create_idx("donations", [("institution_id", 1), ("idempotency_key", 1)], unique=True)
    create_idx("donations", [("institution_id", 1), ("alumni_profile_id", 1)])

    create_coll("engagement_scores")

    # =========================================================================
    # MODULE 12: ADMIN MANAGER
    # =========================================================================
    print("\n--- [Module 12/13] Admin Manager ---")
    create_coll("users")
    create_idx("users", [("institution_id", 1), ("auth.email", 1)], unique=True)
    create_idx("users", [("institution_id", 1), ("profile_type", 1), ("status", 1)])

    create_coll("roles")
    create_idx("roles", [("institution_id", 1), ("code", 1)], unique=True)

    create_coll("permissions")
    create_idx("permissions", [("resource", 1), ("action", 1)], unique=True)

    create_coll("role_assignments")
    create_idx("role_assignments", [("institution_id", 1), ("user_id", 1)])
    create_idx("role_assignments", [("institution_id", 1), ("role_id", 1), ("scope.scope_id", 1)])

    create_coll("system_configs")
    create_idx("system_configs", [("institution_id", 1), ("config_key", 1)], unique=True)

    create_coll("audit_logs")
    create_idx("audit_logs", [("institution_id", 1), ("occurred_at", -1)])
    create_idx("audit_logs", [("institution_id", 1), ("target.document_id", 1)])
    create_idx("audit_logs", [("institution_id", 1), ("event_type", 1), ("occurred_at", -1)])

    create_coll("outbox_events")
    create_idx("outbox_events", [("published", 1), ("created_at", 1)])

    # =========================================================================
    # MODULE 13: COMMUNICATION & ANNOUNCEMENT
    # =========================================================================
    print("\n--- [Module 13/13] Communication & Announcement ---")
    create_coll("announcements")
    create_idx("announcements", [("institution_id", 1), ("published_at", -1)])

    create_coll("campaigns")
    create_idx("campaigns", [("institution_id", 1), ("status", 1)])

    create_coll("notification_logs")
    create_idx("notification_logs", [("institution_id", 1), ("recipient_id", 1), ("sent_at", -1)])

    create_coll("survey_responses")
    create_coll("emergency_alerts")
    create_idx("emergency_alerts", [("institution_id", 1), ("broadcast_at", -1)])

    # =========================================================================
    # SEED DATA
    # =========================================================================
    print("\n--- [Seed] Inserting Default Platform Seed Data ---")
    sample_inst_id = ObjectId("650000000000000000000001")

    roles = [
        {"_id": ObjectId("650000000000000000000101"), "institution_id": sample_inst_id, "code": "SUPER_ADMIN", "name": "Super Administrator", "is_system": True},
        {"_id": ObjectId("650000000000000000000102"), "institution_id": sample_inst_id, "code": "INSTITUTION_ADMIN", "name": "Institution Administrator", "is_system": True},
        {"_id": ObjectId("650000000000000000000103"), "institution_id": sample_inst_id, "code": "FACULTY", "name": "Faculty Member", "is_system": True},
        {"_id": ObjectId("650000000000000000000104"), "institution_id": sample_inst_id, "code": "STUDENT", "name": "Student", "is_system": True}
    ]

    for r in roles:
        db.roles.update_one({"_id": r["_id"]}, {"$setOnInsert": r}, upsert=True)
    print("  [+] System roles initialized.")

    db.system_configs.update_one(
        {"institution_id": sample_inst_id, "config_key": "platform_version"},
        {"$set": {"config_value": "2.0.0", "updated_at": datetime.utcnow()}},
        upsert=True
    )
    print("  [+] System config seed applied.")

    all_colls = db.list_collection_names()
    print("\n============================================================================")
    print(" CampSync MongoDB Atlas Provisioning Completed Successfully!")
    print(f" Total Collections Created/Present: {len(all_colls)}")
    print("============================================================================")

if __name__ == "__main__":
    main()
