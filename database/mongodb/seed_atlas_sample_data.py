"""
============================================================================
CAMPSYNC CORE PLATFORM - SAMPLE DATA SEEDER FOR MONGODB ATLAS
Document Version: 2.0
============================================================================
"""

import sys
import os
import pymongo
from bson.objectid import ObjectId
from datetime import datetime, timedelta

def main():
    uri = "mongodb+srv://campxsync_db_user:y3rySY9LA0DYxktF@campxsync.qrzd0b7.mongodb.net/?appName=CampXSync"
    if len(sys.argv) > 1:
        uri = sys.argv[1]

    print("============================================================================")
    print(" Connecting to MongoDB Atlas for Sample Data Seeding...")
    print("============================================================================")

    client = pymongo.MongoClient(uri)
    db = client.get_database("campsync")

    # Common Referenced ObjectIds
    inst_id = ObjectId("650000000000000000000001")
    
    # Users / Identity
    usr_student1 = ObjectId("650000000000000000000011")
    usr_student2 = ObjectId("650000000000000000000012")
    usr_faculty1 = ObjectId("650000000000000000000021")
    usr_faculty2 = ObjectId("650000000000000000000022")
    usr_staff1   = ObjectId("650000000000000000000031")
    usr_alumni1  = ObjectId("650000000000000000000041")

    # Academics
    prog_cse = ObjectId("650000000000000000000101")
    prog_ece = ObjectId("650000000000000000000102")
    crs_ds   = ObjectId("650000000000000000000111")
    crs_algo = ObjectId("650000000000000000000112")
    term_fall= ObjectId("650000000000000000000121")
    sec_ds_a = ObjectId("650000000000000000000131")
    sec_algo_b = ObjectId("650000000000000000000132")
    enr_1    = ObjectId("650000000000000000000141")
    enr_2    = ObjectId("650000000000000000000142")

    # Exams
    exm_reg1 = ObjectId("650000000000000000000201")
    exm_reg2 = ObjectId("650000000000000000000202")
    exm_tbl1 = ObjectId("650000000000000000000211")

    # Finance
    inv_1    = ObjectId("650000000000000000000301")
    inv_2    = ObjectId("650000000000000000000302")

    # HR
    emp_1    = ObjectId("650000000000000000000401")
    emp_2    = ObjectId("650000000000000000000402")
    dept_cs  = ObjectId("650000000000000000000411")

    # Library
    cat_1    = ObjectId("650000000000000000000501")
    cat_2    = ObjectId("650000000000000000000502")

    # Hostel
    room_101 = ObjectId("650000000000000000000601")
    room_102 = ObjectId("650000000000000000000602")

    # Placement
    comp_1   = ObjectId("650000000000000000000701")
    job_1    = ObjectId("650000000000000000000711")

    # Transport
    route_1  = ObjectId("650000000000000000000801")
    veh_1    = ObjectId("650000000000000000000811")

    # Facilities
    asset_1  = ObjectId("650000000000000000000901")

    # Alumni
    alm_prof1= ObjectId("650000000000000000001001")

    def seed_collection(coll_name, docs):
        coll = db[coll_name]
        inserted_count = 0
        for doc in docs:
            query = {"_id": doc["_id"]} if "_id" in doc else {"institution_id": doc.get("institution_id")}
            try:
                coll.update_one(query, {"$setOnInsert": doc}, upsert=True)
                inserted_count += 1
            except Exception as e:
                pass
        print(f"  [+] Seeded sample rows in: {coll_name}")

    # =========================================================================
    # 1. ACADEMIC MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 1: Academic Management ---")
    seed_collection("programs", [
        {"_id": prog_cse, "institution_id": inst_id, "code": "BTECH-CSE", "name": "B.Tech Computer Science & Engineering", "degree_level": "bachelor", "duration_years": 4},
        {"_id": prog_ece, "institution_id": inst_id, "code": "BTECH-ECE", "name": "B.Tech Electronics & Communication", "degree_level": "bachelor", "duration_years": 4}
    ])
    seed_collection("courses", [
        {"_id": crs_ds, "institution_id": inst_id, "course_code": "CS201", "title": "Data Structures & Algorithms", "credits": 4},
        {"_id": crs_algo, "institution_id": inst_id, "course_code": "CS301", "title": "Design & Analysis of Algorithms", "credits": 4}
    ])
    seed_collection("academic_calendars", [
        {"_id": term_fall, "institution_id": inst_id, "academic_year": "2026-2027", "term_name": "Fall 2026", "start_date": datetime(2026, 8, 1), "end_date": datetime(2026, 12, 20)}
    ])
    seed_collection("course_sections", [
        {"_id": sec_ds_a, "institution_id": inst_id, "course_id": crs_ds, "term_id": term_fall, "faculty_id": usr_faculty1, "capacity": 60, "seats_filled": 2, "status": "open"},
        {"_id": sec_algo_b, "institution_id": inst_id, "course_id": crs_algo, "term_id": term_fall, "faculty_id": usr_faculty2, "capacity": 60, "seats_filled": 1, "status": "open"}
    ])
    seed_collection("class_schedules", [
        {"_id": ObjectId("650000000000000000000151"), "institution_id": inst_id, "course_section_id": sec_ds_a, "day_of_week": 1, "start_time": "09:00", "end_time": "10:00", "room": "LH-101"},
        {"_id": ObjectId("650000000000000000000152"), "institution_id": inst_id, "course_section_id": sec_algo_b, "day_of_week": 2, "start_time": "10:00", "end_time": "11:00", "room": "LH-102"}
    ])
    seed_collection("enrollments", [
        {"_id": enr_1, "institution_id": inst_id, "student_id": usr_student1, "course_section_id": sec_ds_a, "term_id": term_fall, "status": "active", "enrolled_at": datetime.now()},
        {"_id": enr_2, "institution_id": inst_id, "student_id": usr_student2, "course_section_id": sec_ds_a, "term_id": term_fall, "status": "active", "enrolled_at": datetime.now()}
    ])
    seed_collection("learning_pathways", [
        {"_id": ObjectId("650000000000000000000161"), "institution_id": inst_id, "student_id": usr_student1, "recommended_courses": [crs_algo]},
        {"_id": ObjectId("650000000000000000000162"), "institution_id": inst_id, "student_id": usr_student2, "recommended_courses": [crs_ds]}
    ])

    # =========================================================================
    # 2. EXAMINATION MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 2: Examination Management ---")
    seed_collection("exam_timetables", [
        {"_id": exm_tbl1, "institution_id": inst_id, "term_id": term_fall, "exam_date": datetime(2026, 12, 10), "session": "FN"},
        {"_id": ObjectId("650000000000000000000212"), "institution_id": inst_id, "term_id": term_fall, "exam_date": datetime(2026, 12, 12), "session": "AN"}
    ])
    seed_collection("exam_registrations", [
        {"_id": exm_reg1, "institution_id": inst_id, "student_id": usr_student1, "exam_timetable_id": exm_tbl1, "registered_at": datetime.now()},
        {"_id": exm_reg2, "institution_id": inst_id, "student_id": usr_student2, "exam_timetable_id": exm_tbl1, "registered_at": datetime.now()}
    ])
    seed_collection("hall_tickets", [
        {"_id": ObjectId("650000000000000000000221"), "institution_id": inst_id, "exam_registration_id": exm_reg1, "seat_number": "A-12", "venue": "Hall-1"},
        {"_id": ObjectId("650000000000000000000222"), "institution_id": inst_id, "exam_registration_id": exm_reg2, "seat_number": "A-13", "venue": "Hall-1"}
    ])
    seed_collection("question_banks", [
        {"_id": ObjectId("650000000000000000000231"), "institution_id": inst_id, "course_id": crs_ds, "question_text": "Explain Binary Search Tree insertion.", "difficulty_level": "medium"},
        {"_id": ObjectId("650000000000000000000232"), "institution_id": inst_id, "course_id": crs_algo, "question_text": "Explain Dijkstra shortest path algorithm.", "difficulty_level": "hard"}
    ])
    seed_collection("exam_results", [
        {"_id": ObjectId("650000000000000000000241"), "institution_id": inst_id, "exam_registration_id": exm_reg1, "student_id": usr_student1, "course_id": crs_ds, "total_obtained": 88, "total_max": 100, "grade_letter": "A", "grade_points": 9, "status": "published"},
        {"_id": ObjectId("650000000000000000000242"), "institution_id": inst_id, "exam_registration_id": exm_reg2, "student_id": usr_student2, "course_id": crs_ds, "total_obtained": 94, "total_max": 100, "grade_letter": "S", "grade_points": 10, "status": "published"}
    ])
    seed_collection("revaluation_requests", [
        {"_id": ObjectId("650000000000000000000251"), "institution_id": inst_id, "exam_result_id": ObjectId("650000000000000000000241"), "reason": "Doubt in Question 3 evaluation", "status": "pending"}
    ])
    seed_collection("certificates", [
        {"_id": ObjectId("650000000000000000000261"), "institution_id": inst_id, "student_id": usr_student1, "certificate_type": "TRANSCRIPT", "issued_at": datetime.now()}
    ])

    # =========================================================================
    # 3. LEARNING MANAGEMENT (LMS)
    # =========================================================================
    print("\n--- Seeding Module 3: Learning Management (LMS) ---")
    seed_collection("course_content", [
        {"_id": ObjectId("650000000000000000000311"), "institution_id": inst_id, "course_section_id": sec_ds_a, "title": "Module 1: Introduction to Trees", "module_order": 1},
        {"_id": ObjectId("650000000000000000000312"), "institution_id": inst_id, "course_section_id": sec_ds_a, "title": "Module 2: Graph Algorithms", "module_order": 2}
    ])
    seed_collection("assignments", [
        {"_id": ObjectId("650000000000000000000321"), "institution_id": inst_id, "course_section_id": sec_ds_a, "title": "Assignment 1: BST Implementation", "due_date": datetime.now() + timedelta(days=7)},
        {"_id": ObjectId("650000000000000000000322"), "institution_id": inst_id, "course_section_id": sec_ds_a, "title": "Assignment 2: Graph Traversal", "due_date": datetime.now() + timedelta(days=14)}
    ])
    seed_collection("submissions", [
        {"_id": ObjectId("650000000000000000000331"), "institution_id": inst_id, "assignment_id": ObjectId("650000000000000000000321"), "student_id": usr_student1, "attempt_number": 1, "submitted_at": datetime.now(), "status": "graded"},
        {"_id": ObjectId("650000000000000000000332"), "institution_id": inst_id, "assignment_id": ObjectId("650000000000000000000321"), "student_id": usr_student2, "attempt_number": 1, "submitted_at": datetime.now(), "status": "submitted"}
    ])
    seed_collection("discussion_threads", [
        {"_id": ObjectId("650000000000000000000341"), "institution_id": inst_id, "course_section_id": sec_ds_a, "title": "Clarification on Homework 1", "author_id": usr_student1},
        {"_id": ObjectId("650000000000000000000342"), "institution_id": inst_id, "course_section_id": sec_ds_a, "title": "Exam Revision Questions", "author_id": usr_student2}
    ])
    seed_collection("virtual_classrooms", [
        {"_id": ObjectId("650000000000000000000351"), "institution_id": inst_id, "course_section_id": sec_ds_a, "topic": "Live Review Session", "scheduled_start": datetime.now()}
    ])
    seed_collection("progress_tracking", [
        {"_id": ObjectId("650000000000000000000361"), "institution_id": inst_id, "student_id": usr_student1, "course_section_id": sec_ds_a, "completion_percentage": 75.0},
        {"_id": ObjectId("650000000000000000000362"), "institution_id": inst_id, "student_id": usr_student2, "course_section_id": sec_ds_a, "completion_percentage": 90.0}
    ])
    seed_collection("badges", [
        {"_id": ObjectId("650000000000000000000371"), "institution_id": inst_id, "student_id": usr_student1, "badge_code": "TOP_CODER", "title": "Top Coder"}
    ])

    # =========================================================================
    # 4. FINANCE & ACCOUNTS
    # =========================================================================
    print("\n--- Seeding Module 4: Finance & Accounts ---")
    seed_collection("fee_structures", [
        {"_id": ObjectId("650000000000000000000401"), "institution_id": inst_id, "program_id": prog_cse, "academic_year": "2026-2027", "total_fee": 120000.0},
        {"_id": ObjectId("650000000000000000000402"), "institution_id": inst_id, "program_id": prog_ece, "academic_year": "2026-2027", "total_fee": 110000.0}
    ])
    seed_collection("fee_invoices", [
        {"_id": inv_1, "institution_id": inst_id, "student_id": usr_student1, "enrollment_id": enr_1, "fee_structure_id": ObjectId("650000000000000000000401"), "status": "paid", "due_date": datetime(2026, 9, 1)},
        {"_id": inv_2, "institution_id": inst_id, "student_id": usr_student2, "enrollment_id": enr_2, "fee_structure_id": ObjectId("650000000000000000000401"), "status": "issued", "due_date": datetime(2026, 9, 1)}
    ])
    seed_collection("fee_receipts", [
        {"_id": ObjectId("650000000000000000000421"), "institution_id": inst_id, "fee_invoice_id": inv_1, "payment_method": "upi", "receipt_number": "REC-2026-001", "idempotency_key": "IDEM-REC-001"}
    ])
    seed_collection("scholarships", [
        {"_id": ObjectId("650000000000000000000431"), "institution_id": inst_id, "student_id": usr_student1, "title": "Merit Scholarship", "waiver_percentage": 25.0}
    ])
    seed_collection("expenses", [
        {"_id": ObjectId("650000000000000000000441"), "institution_id": inst_id, "category": "LAB_EQUIPMENT", "incurred_date": datetime.now()}
    ])
    seed_collection("budgets", [
        {"_id": ObjectId("650000000000000000000451"), "institution_id": inst_id, "department_id": dept_cs, "financial_year": "2026-2027", "allocated_amount": 500000.0}
    ])
    seed_collection("financial_reports", [
        {"_id": ObjectId("650000000000000000000461"), "institution_id": inst_id, "report_type": "ANNUAL_SUMMARY"}
    ])

    # =========================================================================
    # 5. HUMAN RESOURCE MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 5: Human Resource Management ---")
    seed_collection("employees", [
        {"_id": emp_1, "institution_id": inst_id, "user_id": usr_faculty1, "employee_code": "EMP-101", "department_id": dept_cs, "designation": "Associate Professor", "status": "active"},
        {"_id": emp_2, "institution_id": inst_id, "user_id": usr_faculty2, "employee_code": "EMP-102", "department_id": dept_cs, "designation": "Assistant Professor", "status": "active"}
    ])
    seed_collection("onboarding_cases", [
        {"_id": ObjectId("650000000000000000000511"), "institution_id": inst_id, "employee_id": emp_1, "status": "completed"}
    ])
    seed_collection("leave_requests", [
        {"_id": ObjectId("650000000000000000000521"), "institution_id": inst_id, "employee_id": emp_1, "leave_type": "CASUAL", "status": "approved"}
    ])
    seed_collection("attendance_timesheets", [
        {"_id": ObjectId("650000000000000000000531"), "institution_id": inst_id, "employee_id": emp_1, "date": "2026-08-01", "hours_worked": 8.0},
        {"_id": ObjectId("650000000000000000000532"), "institution_id": inst_id, "employee_id": emp_2, "date": "2026-08-01", "hours_worked": 8.0}
    ])
    seed_collection("performance_reviews", [
        {"_id": ObjectId("650000000000000000000541"), "institution_id": inst_id, "employee_id": emp_1, "period": "2025-2026", "rating": "EXCEEDS_EXPECTATIONS"}
    ])
    seed_collection("training_records", [
        {"_id": ObjectId("650000000000000000000551"), "institution_id": inst_id, "employee_id": emp_1, "course_title": "Advanced AI Pedagogy"}
    ])
    seed_collection("payroll_runs", [
        {"_id": ObjectId("650000000000000000000561"), "institution_id": inst_id, "employee_id": emp_1, "period": "2026-07", "status": "disbursed", "idempotency_key": "IDEM-PAY-001"}
    ])

    # =========================================================================
    # 6. LIBRARY MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 6: Library Management ---")
    seed_collection("catalog_items", [
        {"_id": cat_1, "institution_id": inst_id, "title": "Introduction to Algorithms (CLRS)", "isbn": "9780262033848", "total_copies": 10, "available_copies": 8},
        {"_id": cat_2, "institution_id": inst_id, "title": "Clean Code", "isbn": "9780132350884", "total_copies": 5, "available_copies": 4}
    ])
    seed_collection("book_circulations", [
        {"_id": ObjectId("650000000000000000000611"), "institution_id": inst_id, "catalog_item_id": cat_1, "member_id": usr_student1, "status": "issued", "issued_at": datetime.now()},
        {"_id": ObjectId("650000000000000000000612"), "institution_id": inst_id, "catalog_item_id": cat_2, "member_id": usr_student2, "status": "returned", "issued_at": datetime.now()}
    ])
    seed_collection("digital_library_assets", [
        {"_id": ObjectId("650000000000000000000621"), "institution_id": inst_id, "title": "IEEE Data Structures Journal E-Book"}
    ])
    seed_collection("memberships", [
        {"_id": ObjectId("650000000000000000000631"), "institution_id": inst_id, "user_id": usr_student1, "status": "active"},
        {"_id": ObjectId("650000000000000000000632"), "institution_id": inst_id, "user_id": usr_student2, "status": "active"}
    ])
    seed_collection("fines", [
        {"_id": ObjectId("650000000000000000000641"), "institution_id": inst_id, "member_id": usr_student1, "amount": 50.0, "status": "unpaid"}
    ])

    # =========================================================================
    # 7. HOSTEL MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 7: Hostel Management ---")
    seed_collection("rooms", [
        {"_id": room_101, "institution_id": inst_id, "hostel_name": "Tagore Hostel", "room_number": "101", "capacity": 2, "occupied_beds": 1},
        {"_id": room_102, "institution_id": inst_id, "hostel_name": "Tagore Hostel", "room_number": "102", "capacity": 2, "occupied_beds": 1}
    ])
    seed_collection("room_allocations", [
        {"_id": ObjectId("650000000000000000000711"), "institution_id": inst_id, "student_id": usr_student1, "room_id": room_101, "academic_year": "2026-2027", "status": "active"},
        {"_id": ObjectId("650000000000000000000712"), "institution_id": inst_id, "student_id": usr_student2, "room_id": room_102, "academic_year": "2026-2027", "status": "active"}
    ])
    seed_collection("hostel_checkins", [
        {"_id": ObjectId("650000000000000000000721"), "institution_id": inst_id, "student_id": usr_student1, "direction": "in", "recorded_at": datetime.now()},
        {"_id": ObjectId("650000000000000000000722"), "institution_id": inst_id, "student_id": usr_student2, "direction": "in", "recorded_at": datetime.now()}
    ])
    seed_collection("mess_plans", [
        {"_id": ObjectId("650000000000000000000731"), "institution_id": inst_id, "student_id": usr_student1, "plan_type": "veg", "status": "active"},
        {"_id": ObjectId("650000000000000000000732"), "institution_id": inst_id, "student_id": usr_student2, "plan_type": "non_veg", "status": "active"}
    ])
    seed_collection("visitor_logs", [
        {"_id": ObjectId("650000000000000000000741"), "institution_id": inst_id, "visitor_name": "Parent of Student 1", "student_id": usr_student1}
    ])
    seed_collection("maintenance_requests", [
        {"_id": ObjectId("650000000000000000000751"), "institution_id": inst_id, "room_id": room_101, "issue": "Fan Repair Needed", "status": "open"}
    ])

    # =========================================================================
    # 8. PLACEMENT MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 8: Placement Management ---")
    seed_collection("companies", [
        {"_id": comp_1, "institution_id": inst_id, "name": "Google", "industry": "Technology"},
        {"_id": ObjectId("650000000000000000000802"), "institution_id": inst_id, "name": "Microsoft", "industry": "Technology"}
    ])
    seed_collection("job_postings", [
        {"_id": job_1, "institution_id": inst_id, "company_id": comp_1, "title": "Software Development Engineer", "status": "open"},
        {"_id": ObjectId("650000000000000000000812"), "institution_id": inst_id, "company_id": comp_1, "title": "Data Scientist", "status": "open"}
    ])
    seed_collection("placement_drives", [
        {"_id": ObjectId("650000000000000000000821"), "institution_id": inst_id, "name": "Campus Recruitment Drive 2026"}
    ])
    seed_collection("student_applications", [
        {"_id": ObjectId("650000000000000000000831"), "institution_id": inst_id, "student_id": usr_student1, "job_posting_id": job_1, "status": "shortlisted"},
        {"_id": ObjectId("650000000000000000000832"), "institution_id": inst_id, "student_id": usr_student2, "job_posting_id": job_1, "status": "applied"}
    ])
    seed_collection("interview_schedules", [
        {"_id": ObjectId("650000000000000000000841"), "institution_id": inst_id, "student_id": usr_student1, "round": "Technical Round 1"}
    ])
    seed_collection("offers", [
        {"_id": ObjectId("650000000000000000000851"), "institution_id": inst_id, "student_application_id": ObjectId("650000000000000000000831"), "status": "extended"}
    ])

    # =========================================================================
    # 9. TRANSPORT MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 9: Transport Management ---")
    seed_collection("routes", [
        {"_id": route_1, "institution_id": inst_id, "route_code": "R-10", "name": "North Campus Shuttle"},
        {"_id": ObjectId("650000000000000000000902"), "institution_id": inst_id, "route_code": "R-20", "name": "South City Express"}
    ])
    seed_collection("vehicles", [
        {"_id": veh_1, "institution_id": inst_id, "registration_number": "KA-01-EQ-1234", "type": "bus", "capacity": 50},
        {"_id": ObjectId("650000000000000000000912"), "institution_id": inst_id, "registration_number": "KA-01-EQ-5678", "type": "bus", "capacity": 50}
    ])
    seed_collection("vehicle_assignments", [
        {"_id": ObjectId("650000000000000000000921"), "institution_id": inst_id, "vehicle_id": veh_1, "route_id": route_1, "driver_id": usr_staff1}
    ])
    seed_collection("driver_profiles", [
        {"_id": usr_staff1, "institution_id": inst_id, "driver_name": "Ramesh Kumar", "license_number": "DL-99887766"}
    ])
    seed_collection("gps_pings", [
        {"ts": datetime.now(), "meta": {"institution_id": inst_id, "vehicle_id": veh_1}, "location": {"type": "Point", "coordinates": [77.5946, 12.9716]}, "speed_kmph": 35.0},
        {"ts": datetime.now() - timedelta(seconds=30), "meta": {"institution_id": inst_id, "vehicle_id": veh_1}, "location": {"type": "Point", "coordinates": [77.5950, 12.9720]}, "speed_kmph": 40.0}
    ])

    # =========================================================================
    # 10. FACILITIES MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 10: Facilities Management ---")
    seed_collection("assets", [
        {"_id": asset_1, "institution_id": inst_id, "asset_tag": "AST-1001", "name": "High Performance Server Rack"},
        {"_id": ObjectId("650000000000000000001002"), "institution_id": inst_id, "asset_tag": "AST-1002", "name": "4K Projector Auditorium"}
    ])
    seed_collection("work_orders", [
        {"_id": ObjectId("650000000000000000001011"), "institution_id": inst_id, "asset_id": asset_1, "priority": "high", "status": "assigned"},
        {"_id": ObjectId("650000000000000000001012"), "institution_id": inst_id, "asset_id": ObjectId("650000000000000000001002"), "priority": "medium", "status": "resolved"}
    ])
    seed_collection("inventory_items", [
        {"_id": ObjectId("650000000000000000001021"), "institution_id": inst_id, "sku": "SKU-CAT6-CABLE", "name": "Cat6 Ethernet Cable", "quantity_on_hand": 500},
        {"_id": ObjectId("650000000000000000001022"), "institution_id": inst_id, "sku": "SKU-LED-BULB", "name": "Philips 15W LED Bulb", "quantity_on_hand": 200}
    ])
    seed_collection("vendors", [
        {"_id": ObjectId("650000000000000000001031"), "institution_id": inst_id, "vendor_name": "Dell Enterprise Solutions"}
    ])
    seed_collection("utility_readings", [
        {"_id": ObjectId("650000000000000000001041"), "institution_id": inst_id, "meter_type": "ELECTRICITY", "reading_value": 45210.5}
    ])
    seed_collection("preventive_maintenance_schedules", [
        {"_id": ObjectId("650000000000000000001051"), "institution_id": inst_id, "asset_id": asset_1, "frequency": "MONTHLY"}
    ])

    # =========================================================================
    # 11. ALUMNI MANAGEMENT
    # =========================================================================
    print("\n--- Seeding Module 11: Alumni Management ---")
    seed_collection("alumni_profiles", [
        {"_id": alm_prof1, "institution_id": inst_id, "user_id": usr_alumni1, "graduation_year": 2024, "current_employer": "Amazon"},
        {"_id": ObjectId("650000000000000000001102"), "institution_id": inst_id, "user_id": ObjectId("650000000000000000000042"), "graduation_year": 2023, "current_employer": "Meta"}
    ])
    seed_collection("alumni_events", [
        {"_id": ObjectId("650000000000000000001111"), "institution_id": inst_id, "event_name": "Annual Alumni Meet 2026"}
    ])
    seed_collection("mentorship_pairs", [
        {"_id": ObjectId("650000000000000000001121"), "institution_id": inst_id, "alumni_profile_id": alm_prof1, "student_id": usr_student1}
    ])
    seed_collection("donations", [
        {"_id": ObjectId("650000000000000000001131"), "institution_id": inst_id, "alumni_profile_id": alm_prof1, "amount": 10000.0, "idempotency_key": "DON-001"}
    ])
    seed_collection("engagement_scores", [
        {"_id": ObjectId("650000000000000000001141"), "institution_id": inst_id, "alumni_profile_id": alm_prof1, "score": 92.5}
    ])

    # =========================================================================
    # 12. ADMIN MANAGER
    # =========================================================================
    print("\n--- Seeding Module 12: Admin Manager ---")
    seed_collection("users", [
        {"_id": usr_student1, "institution_id": inst_id, "auth": {"email": "john.student@campx.com"}, "profile_type": "student", "status": "active"},
        {"_id": usr_student2, "institution_id": inst_id, "auth": {"email": "alice.student@campx.com"}, "profile_type": "student", "status": "active"},
        {"_id": usr_faculty1, "institution_id": inst_id, "auth": {"email": "prof.dr.smith@campx.com"}, "profile_type": "faculty", "status": "active"},
        {"_id": usr_faculty2, "institution_id": inst_id, "auth": {"email": "prof.dr.jones@campx.com"}, "profile_type": "faculty", "status": "active"}
    ])
    seed_collection("permissions", [
        {"_id": ObjectId("650000000000000000001211"), "resource": "course", "action": "read"},
        {"_id": ObjectId("650000000000000000001212"), "resource": "course", "action": "write"}
    ])
    seed_collection("role_assignments", [
        {"_id": ObjectId("650000000000000000001221"), "institution_id": inst_id, "user_id": usr_student1, "role_id": ObjectId("650000000000000000000104")},
        {"_id": ObjectId("650000000000000000001222"), "institution_id": inst_id, "user_id": usr_faculty1, "role_id": ObjectId("650000000000000000000103")}
    ])
    seed_collection("audit_logs", [
        {"_id": ObjectId("650000000000000000001231"), "institution_id": inst_id, "event_type": "EnrollmentCreated", "occurred_at": datetime.now()},
        {"_id": ObjectId("650000000000000000001232"), "institution_id": inst_id, "event_type": "FeePaid", "occurred_at": datetime.now()}
    ])
    seed_collection("outbox_events", [
        {"_id": ObjectId("650000000000000000001241"), "event_type": "EnrollmentCreated", "published": True, "created_at": datetime.now()}
    ])

    # =========================================================================
    # 13. COMMUNICATION & ANNOUNCEMENT
    # =========================================================================
    print("\n--- Seeding Module 13: Communication & Announcement ---")
    seed_collection("announcements", [
        {"_id": ObjectId("650000000000000000001301"), "institution_id": inst_id, "title": "Fall 2026 Orientation Schedule Announced", "published_at": datetime.now()},
        {"_id": ObjectId("650000000000000000001302"), "institution_id": inst_id, "title": "Library Extended Hours for Midterms", "published_at": datetime.now()}
    ])
    seed_collection("campaigns", [
        {"_id": ObjectId("650000000000000000001311"), "institution_id": inst_id, "name": "Fee Deadline Reminder Campaign", "status": "running"}
    ])
    seed_collection("notification_logs", [
        {"_id": ObjectId("650000000000000000001321"), "institution_id": inst_id, "recipient_id": usr_student1, "channel": "email", "status": "delivered", "sent_at": datetime.now()},
        {"_id": ObjectId("650000000000000000001322"), "institution_id": inst_id, "recipient_id": usr_student2, "channel": "push", "status": "delivered", "sent_at": datetime.now()}
    ])
    seed_collection("survey_responses", [
        {"_id": ObjectId("650000000000000000001331"), "institution_id": inst_id, "survey_id": ObjectId("650000000000000000001330"), "user_id": usr_student1}
    ])
    seed_collection("emergency_alerts", [
        {"_id": ObjectId("650000000000000000001341"), "institution_id": inst_id, "message": "Campus Heavy Rain Advisory: Classes moved online today.", "severity": "warning", "broadcast_at": datetime.now()}
    ])

    print("\n============================================================================")
    print(" Sample Data Seeding Completed Successfully Across All 13 Modules!")
    print("============================================================================")

if __name__ == "__main__":
    main()
