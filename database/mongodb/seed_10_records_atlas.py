"""
============================================================================
CAMPSYNC CORE PLATFORM - 10-RECORDS PER COLLECTION ATLAS SEEDER
Document Version: 2.0 (All 13 Core Modules, ~81 Collections x 10 Records)
============================================================================
"""

import sys
import os
import pymongo
from bson.objectid import ObjectId
from datetime import datetime, timedelta
import random

def oid(module_num, index):
    """Generates a deterministic 24-char hex ObjectId for reproducible relations."""
    return ObjectId(f"650000000000{module_num:04d}0000{index:04d}")

def main():
    uri = "mongodb+srv://campxsync_db_user:y3rySY9LA0DYxktF@campxsync.qrzd0b7.mongodb.net/?appName=CampXSync"
    if len(sys.argv) > 1:
        uri = sys.argv[1]

    print("============================================================================")
    print(" Connecting to MongoDB Atlas for 10-Record Bulk Seeding...")
    print("============================================================================")

    client = pymongo.MongoClient(uri)
    db = client.get_database("campsync")

    inst_id = oid(0, 1)

    def seed(coll_name, docs):
        coll = db[coll_name]
        inserted = 0
        for doc in docs:
            query = {"_id": doc["_id"]} if "_id" in doc else {"institution_id": doc.get("institution_id"), "ts": doc.get("ts")}
            try:
                coll.update_one(query, {"$set": doc}, upsert=True)
                inserted += 1
            except Exception as e:
                pass
        print(f"  [+] Seeded {len(docs)} records in: {coll_name}")

    # =========================================================================
    # MODULE 12: ADMIN MANAGER (Users, Roles, Identity) - Seed first for references
    # =========================================================================
    print("\n--- [1/13] Admin Manager (Users & Identity) ---")
    users = []
    user_types = ["student", "student", "student", "student", "faculty", "faculty", "faculty", "staff", "staff", "alumni"]
    names = ["John Doe", "Alice Smith", "Bob Johnson", "Emma Watson", "Dr. Alan Turing", "Dr. Grace Hopper", "Dr. Richard Feynman", "Robert Taylor", "Sarah Jenkins", "Michael Brown"]
    emails = ["john.d@campx.com", "alice.s@campx.com", "bob.j@campx.com", "emma.w@campx.com", "alan.t@campx.com", "grace.h@campx.com", "richard.f@campx.com", "robert.t@campx.com", "sarah.j@campx.com", "michael.b@campx.com"]
    
    for i in range(10):
        users.append({
            "_id": oid(12, i+1),
            "institution_id": inst_id,
            "auth": {"email": emails[i], "password_hash": "$2a$10$e7x...dummyhash"},
            "profile_type": user_types[i],
            "profile": {"name": names[i], "phone": f"+91 98765 4321{i}"},
            "status": "active",
            "created_at": datetime.now() - timedelta(days=300)
        })
    seed("users", users)

    roles = []
    role_codes = ["SUPER_ADMIN", "INSTITUTION_ADMIN", "FACULTY", "STUDENT", "DEAN", "HOD", "REGISTRAR", "WARDEN", "LIBRARIAN", "ACCOUNTANT"]
    for i in range(10):
        roles.append({
            "_id": oid(12, 100 + i + 1),
            "institution_id": inst_id,
            "code": role_codes[i],
            "name": role_codes[i].replace("_", " ").title(),
            "is_system": True
        })
    seed("roles", roles)

    permissions = []
    actions = ["read", "write", "delete", "export", "approve", "moderate", "audit", "publish", "archive", "admin"]
    for i in range(10):
        permissions.append({
            "_id": oid(12, 200 + i + 1),
            "resource": "academic_records",
            "action": actions[i]
        })
    seed("permissions", permissions)

    role_assigns = []
    for i in range(10):
        role_assigns.append({
            "_id": oid(12, 300 + i + 1),
            "institution_id": inst_id,
            "user_id": oid(12, i+1),
            "role_id": oid(12, 100 + (i % 4) + 1),
            "granted_at": datetime.now() - timedelta(days=100)
        })
    seed("role_assignments", role_assigns)

    sys_configs = []
    config_keys = ["platform_version", "max_login_attempts", "jwt_ttl_seconds", "mfa_required", "theme_color", "currency_symbol", "academic_year", "timezone", "file_upload_limit_mb", "maintenance_mode"]
    config_vals = ["2.0.0", "5", "86400", "false", "#1E40AF", "INR", "2026-2027", "Asia/Kolkata", "50", "false"]
    for i in range(10):
        sys_configs.append({
            "_id": oid(12, 400 + i + 1),
            "institution_id": inst_id,
            "config_key": config_keys[i],
            "config_value": config_vals[i],
            "updated_at": datetime.now()
        })
    seed("system_configs", sys_configs)

    audit_logs = []
    event_types = ["EnrollmentCreated", "FeePaid", "ExamResultPublished", "UserLoggedIn", "RoleAssigned", "CourseCreated", "PayrollDisbursed", "GradeUpdated", "HostelCheckIn", "BookIssued"]
    for i in range(10):
        audit_logs.append({
            "_id": oid(12, 500 + i + 1),
            "institution_id": inst_id,
            "event_type": event_types[i],
            "actor": {"user_id": oid(12, (i%4)+1), "ip_address": f"192.168.1.{10+i}"},
            "occurred_at": datetime.now() - timedelta(hours=i*5)
        })
    seed("audit_logs", audit_logs)

    outbox_events = []
    for i in range(10):
        outbox_events.append({
            "_id": oid(12, 600 + i + 1),
            "event_type": event_types[i],
            "published": True,
            "created_at": datetime.now() - timedelta(hours=i*5)
        })
    seed("outbox_events", outbox_events)

    # =========================================================================
    # MODULE 1: ACADEMIC MANAGEMENT
    # =========================================================================
    print("\n--- [2/13] Academic Management ---")
    programs = []
    prog_codes = ["BTECH-CSE", "BTECH-ECE", "BTECH-ME", "BTECH-CE", "MBA", "MCA", "MTECH-CSE", "BSC-CS", "BCA", "PHD-CS"]
    prog_names = ["B.Tech Computer Science", "B.Tech Electronics", "B.Tech Mechanical", "B.Tech Civil", "Master of Business Admin", "Master of Computer App", "M.Tech Computer Science", "B.Sc Computer Science", "Bachelor of Computer App", "Ph.D. Computer Science"]
    for i in range(10):
        programs.append({
            "_id": oid(1, i+1),
            "institution_id": inst_id,
            "code": prog_codes[i],
            "name": prog_names[i],
            "degree_level": "bachelor" if i < 4 else ("master" if i < 9 else "doctorate"),
            "duration_years": 4 if i < 4 else (2 if i < 9 else 3)
        })
    seed("programs", programs)

    courses = []
    crs_codes = ["CS201", "CS301", "CS302", "CS401", "CS402", "EC201", "EC301", "ME201", "MBA101", "MCA201"]
    crs_titles = ["Data Structures", "Design & Analysis of Algorithms", "Database Management Systems", "Operating Systems", "Computer Networks", "Digital Electronics", "Microprocessors", "Thermodynamics", "Financial Accounting", "Web Technologies"]
    for i in range(10):
        courses.append({
            "_id": oid(1, 100 + i + 1),
            "institution_id": inst_id,
            "course_code": crs_codes[i],
            "title": crs_titles[i],
            "credits": 4.0
        })
    seed("courses", courses)

    acad_calendars = []
    terms = ["Fall 2024", "Spring 2025", "Fall 2025", "Spring 2026", "Fall 2026", "Spring 2027", "Fall 2027", "Spring 2028", "Fall 2028", "Spring 2029"]
    for i in range(10):
        acad_calendars.append({
            "_id": oid(1, 200 + i + 1),
            "institution_id": inst_id,
            "academic_year": f"202{4+(i//2)}-202{5+(i//2)}",
            "term_name": terms[i],
            "start_date": datetime(2024 + (i//2), 8 if i%2==0 else 1, 1),
            "end_date": datetime(2024 + (i//2), 12 if i%2==0 else 5, 20)
        })
    seed("academic_calendars", acad_calendars)

    course_sections = []
    for i in range(10):
        course_sections.append({
            "_id": oid(1, 300 + i + 1),
            "institution_id": inst_id,
            "course_id": oid(1, 100 + i + 1),
            "term_id": oid(1, 200 + 5), # Fall 2026
            "faculty_id": oid(12, 5 + (i % 3)), # Faculty Alan/Grace/Richard
            "capacity": 60,
            "seats_filled": 35 + i,
            "status": "open"
        })
    seed("course_sections", course_sections)

    schedules = []
    rooms = ["LH-101", "LH-102", "LH-103", "LH-104", "LAB-201", "LAB-202", "LAB-203", "AUDI-1", "AUDI-2", "SEMINAR-1"]
    for i in range(10):
        schedules.append({
            "_id": oid(1, 400 + i + 1),
            "institution_id": inst_id,
            "course_section_id": oid(1, 300 + i + 1),
            "day_of_week": (i % 5) + 1,
            "start_time": f"{9+(i%4)}:00",
            "end_time": f"{10+(i%4)}:00",
            "room_id": rooms[i]
        })
    seed("class_schedules", schedules)

    enrollments = []
    for i in range(10):
        enrollments.append({
            "_id": oid(1, 500 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1), # Students John/Alice/Bob/Emma
            "course_section_id": oid(1, 300 + (i % 5) + 1),
            "term_id": oid(1, 200 + 5),
            "status": "active",
            "enrolled_at": datetime.now() - timedelta(days=30)
        })
    seed("enrollments", enrollments)

    pathways = []
    for i in range(10):
        pathways.append({
            "_id": oid(1, 600 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "recommended_courses": [oid(1, 100 + ((i+1)%10) + 1), oid(1, 100 + ((i+2)%10) + 1)]
        })
    seed("learning_pathways", pathways)

    # =========================================================================
    # 3. EXAMINATION MANAGEMENT
    # =========================================================================
    print("\n--- [3/13] Examination Management ---")
    exam_timetables = []
    for i in range(10):
        exam_timetables.append({
            "_id": oid(2, i+1),
            "institution_id": inst_id,
            "term_id": oid(1, 200 + 5),
            "exam_date": datetime(2026, 12, 1 + i),
            "session": "FN" if i % 2 == 0 else "AN"
        })
    seed("exam_timetables", exam_timetables)

    exam_regs = []
    for i in range(10):
        exam_regs.append({
            "_id": oid(2, 100 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "exam_timetable_id": oid(2, i+1),
            "registered_at": datetime.now() - timedelta(days=15)
        })
    seed("exam_registrations", exam_regs)

    hall_tickets = []
    for i in range(10):
        hall_tickets.append({
            "_id": oid(2, 200 + i + 1),
            "institution_id": inst_id,
            "exam_registration_id": oid(2, 100 + i + 1),
            "seat_number": f"HALL-1-{100+i}",
            "venue": f"Exam Hall { (i%3) + 1 }"
        })
    seed("hall_tickets", hall_tickets)

    question_banks = []
    for i in range(10):
        question_banks.append({
            "_id": oid(2, 300 + i + 1),
            "institution_id": inst_id,
            "course_id": oid(1, 100 + i + 1),
            "question_text": f"Question {i+1}: Explain theoretical design aspects of {crs_titles[i]}.",
            "difficulty_level": "easy" if i%3==0 else ("medium" if i%3==1 else "hard")
        })
    seed("question_banks", question_banks)

    exam_results = []
    grades = ["S", "A", "B", "C", "D", "S", "A", "B", "A", "S"]
    points = [10, 9, 8, 7, 6, 10, 9, 8, 9, 10]
    for i in range(10):
        exam_results.append({
            "_id": oid(2, 400 + i + 1),
            "institution_id": inst_id,
            "exam_registration_id": oid(2, 100 + i + 1),
            "student_id": oid(12, (i % 4) + 1),
            "course_id": oid(1, 100 + i + 1),
            "total_obtained": 70 + (i * 3),
            "total_max": 100,
            "grade_letter": grades[i],
            "grade_points": points[i],
            "status": "published"
        })
    seed("exam_results", exam_results)

    revaluations = []
    for i in range(10):
        revaluations.append({
            "_id": oid(2, 500 + i + 1),
            "institution_id": inst_id,
            "exam_result_id": oid(2, 400 + i + 1),
            "reason": f"Requesting recount for Question {i+1}",
            "status": "pending" if i % 2 == 0 else "approved"
        })
    seed("revaluation_requests", revaluations)

    certs = []
    cert_types = ["TRANSCRIPT", "DEGREE", "MERIT", "BONAFIDE", "PROVISIONAL", "TRANSCRIPT", "DEGREE", "MERIT", "BONAFIDE", "PROVISIONAL"]
    for i in range(10):
        certs.append({
            "_id": oid(2, 600 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "certificate_type": cert_types[i],
            "issued_at": datetime.now() - timedelta(days=i*10)
        })
    seed("certificates", certs)

    # =========================================================================
    # 4. LEARNING MANAGEMENT (LMS)
    # =========================================================================
    print("\n--- [4/13] Learning Management (LMS) ---")
    course_content = []
    for i in range(10):
        course_content.append({
            "_id": oid(3, i+1),
            "institution_id": inst_id,
            "course_section_id": oid(1, 300 + (i % 5) + 1),
            "title": f"Module {i+1}: Advanced Concepts Part {i+1}",
            "module_order": i + 1
        })
    seed("course_content", course_content)

    assignments = []
    for i in range(10):
        assignments.append({
            "_id": oid(3, 100 + i + 1),
            "institution_id": inst_id,
            "course_section_id": oid(1, 300 + (i % 5) + 1),
            "title": f"Assignment {i+1}: Practical Lab Implementation {i+1}",
            "due_date": datetime.now() + timedelta(days=i+5)
        })
    seed("assignments", assignments)

    submissions = []
    for i in range(10):
        submissions.append({
            "_id": oid(3, 200 + i + 1),
            "institution_id": inst_id,
            "assignment_id": oid(3, 100 + i + 1),
            "student_id": oid(12, (i % 4) + 1),
            "attempt_number": 1,
            "submitted_at": datetime.now() - timedelta(days=i),
            "status": "graded" if i % 2 == 0 else "submitted"
        })
    seed("submissions", submissions)

    threads = []
    for i in range(10):
        threads.append({
            "_id": oid(3, 300 + i + 1),
            "institution_id": inst_id,
            "course_section_id": oid(1, 300 + (i % 5) + 1),
            "title": f"Discussion Topic {i+1}: Understanding Chapter {i+1}",
            "author_id": oid(12, (i % 4) + 1)
        })
    seed("discussion_threads", threads)

    vclassrooms = []
    for i in range(10):
        vclassrooms.append({
            "_id": oid(3, 400 + i + 1),
            "institution_id": inst_id,
            "course_section_id": oid(1, 300 + (i % 5) + 1),
            "topic": f"Live Q&A Session {i+1}",
            "scheduled_start": datetime.now() + timedelta(days=i)
        })
    seed("virtual_classrooms", vclassrooms)

    progress = []
    for i in range(10):
        progress.append({
            "_id": oid(3, 500 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "course_section_id": oid(1, 300 + (i % 5) + 1),
            "completion_percentage": 50.0 + (i * 5)
        })
    seed("progress_tracking", progress)

    badges = []
    badge_titles = ["Top Coder", "Quick Learner", "Forum Contributor", "Bug Hunter", "Clean Code Master", "Fast Submission", "Peer Mentor", "Perfect Attendance", "Quiz Champion", "LMS Scholar"]
    for i in range(10):
        badges.append({
            "_id": oid(3, 600 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "badge_code": f"BADGE_{i+1}",
            "title": badge_titles[i]
        })
    seed("badges", badges)

    # =========================================================================
    # 5. FINANCE & ACCOUNTS
    # =========================================================================
    print("\n--- [5/13] Finance & Accounts ---")
    fee_structs = []
    for i in range(10):
        fee_structs.append({
            "_id": oid(4, i+1),
            "institution_id": inst_id,
            "program_id": oid(1, i+1),
            "academic_year": "2026-2027",
            "total_fee": 100000.0 + (i * 5000)
        })
    seed("fee_structures", fee_structs)

    invoices = []
    statuses = ["paid", "issued", "partially_paid", "overdue", "paid", "issued", "paid", "partially_paid", "paid", "issued"]
    for i in range(10):
        invoices.append({
            "_id": oid(4, 100 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "enrollment_id": oid(1, 500 + i + 1),
            "fee_structure_id": oid(4, (i%10)+1),
            "status": statuses[i],
            "due_date": datetime(2026, 9, 15)
        })
    seed("fee_invoices", invoices)

    receipts = []
    methods = ["upi", "card", "netbanking", "dd", "cash", "upi", "card", "upi", "netbanking", "card"]
    for i in range(10):
        receipts.append({
            "_id": oid(4, 200 + i + 1),
            "institution_id": inst_id,
            "fee_invoice_id": oid(4, 100 + i + 1),
            "payment_method": methods[i],
            "receipt_number": f"REC-2026-{100+i}",
            "idempotency_key": f"IDEM-REC-{100+i}"
        })
    seed("fee_receipts", receipts)

    scholarships = []
    for i in range(10):
        scholarships.append({
            "_id": oid(4, 300 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "title": f"Scholarship Scheme {i+1}",
            "waiver_percentage": 10.0 + (i * 5)
        })
    seed("scholarships", scholarships)

    expenses = []
    categories = ["LAB_EQUIPMENT", "FACULTY_SALARY", "UTILITIES", "MAINTENANCE", "LIBRARY_BOOKS", "SOFTWARE_LICENSES", "EVENTS", "SPORTS", "HOSTEL_SUPPLIES", "TRANSPORT_FUEL"]
    for i in range(10):
        expenses.append({
            "_id": oid(4, 400 + i + 1),
            "institution_id": inst_id,
            "category": categories[i],
            "incurred_date": datetime.now() - timedelta(days=i*3)
        })
    seed("expenses", expenses)

    budgets = []
    for i in range(10):
        budgets.append({
            "_id": oid(4, 500 + i + 1),
            "institution_id": inst_id,
            "department_id": oid(5, 100 + i + 1),
            "financial_year": "2026-2027",
            "allocated_amount": 500000.0 + (i * 100000)
        })
    seed("budgets", budgets)

    reports = []
    report_types = ["ANNUAL_FINANCIAL", "TAX_AUDIT", "FEE_COLLECTION", "EXPENSE_ANALYSIS", "BUDGET_VARIANCE", "PAYROLL_SUMMARY", "SCHOLARSHIP_DISBURSEMENT", "DEPARTMENT_BALANCE", "VENDOR_PAYMENT", "ASSET_DEPRECIATION"]
    for i in range(10):
        reports.append({
            "_id": oid(4, 600 + i + 1),
            "institution_id": inst_id,
            "report_type": report_types[i]
        })
    seed("financial_reports", reports)

    # =========================================================================
    # 6. HUMAN RESOURCE MANAGEMENT
    # =========================================================================
    print("\n--- [6/13] Human Resource Management ---")
    employees = []
    designations = ["Professor", "Associate Professor", "Assistant Professor", "Lab Technician", "System Admin", "HOD", "Registrar", "Warden", "Accountant", "Librarian"]
    for i in range(10):
        employees.append({
            "_id": oid(5, i+1),
            "institution_id": inst_id,
            "user_id": oid(12, i+1),
            "employee_code": f"EMP-2026-{100+i}",
            "department_id": oid(5, 100 + (i % 3) + 1),
            "designation": designations[i],
            "status": "active"
        })
    seed("employees", employees)

    onboarding = []
    for i in range(10):
        onboarding.append({
            "_id": oid(5, 100 + i + 1),
            "institution_id": inst_id,
            "employee_id": oid(5, i+1),
            "status": "completed" if i % 2 == 0 else "in_progress"
        })
    seed("onboarding_cases", onboarding)

    leaves = []
    for i in range(10):
        leaves.append({
            "_id": oid(5, 200 + i + 1),
            "institution_id": inst_id,
            "employee_id": oid(5, i+1),
            "leave_type": "CASUAL" if i%2==0 else "SICK",
            "status": "approved"
        })
    seed("leave_requests", leaves)

    timesheets = []
    for i in range(10):
        timesheets.append({
            "_id": oid(5, 300 + i + 1),
            "institution_id": inst_id,
            "employee_id": oid(5, i+1),
            "date": f"2026-08-0{i+1}" if i<9 else "2026-08-10",
            "hours_worked": 8.0
        })
    seed("attendance_timesheets", timesheets)

    perf_reviews = []
    ratings = ["EXCEEDS_EXPECTATIONS", "MEETS_EXPECTATIONS", "OUTSTANDING", "MEETS_EXPECTATIONS", "EXCEEDS_EXPECTATIONS", "OUTSTANDING", "MEETS_EXPECTATIONS", "EXCEEDS_EXPECTATIONS", "MEETS_EXPECTATIONS", "OUTSTANDING"]
    for i in range(10):
        perf_reviews.append({
            "_id": oid(5, 400 + i + 1),
            "institution_id": inst_id,
            "employee_id": oid(5, i+1),
            "period": "2025-2026",
            "rating": ratings[i]
        })
    seed("performance_reviews", perf_reviews)

    trainings = []
    for i in range(10):
        trainings.append({
            "_id": oid(5, 500 + i + 1),
            "institution_id": inst_id,
            "employee_id": oid(5, i+1),
            "course_title": f"Professional Pedagogy Module {i+1}"
        })
    seed("training_records", trainings)

    payrolls = []
    for i in range(10):
        payrolls.append({
            "_id": oid(5, 600 + i + 1),
            "institution_id": inst_id,
            "employee_id": oid(5, i+1),
            "period": "2026-07",
            "status": "disbursed",
            "idempotency_key": f"IDEM-PAY-{100+i}"
        })
    seed("payroll_runs", payrolls)

    # =========================================================================
    # 7. LIBRARY MANAGEMENT
    # =========================================================================
    print("\n--- [7/13] Library Management ---")
    catalog = []
    books = [
        ("Introduction to Algorithms", "9780262033848"),
        ("Clean Code", "9780132350884"),
        ("Design Patterns: GoF", "9780201633610"),
        ("Artificial Intelligence: A Modern Approach", "9780134610993"),
        ("Database System Concepts", "9780073523323"),
        ("Computer Networks: Tanenbaum", "9780132126953"),
        ("Operating System Concepts: Silberschatz", "9781118063330"),
        ("Compilers: Principles & Tools", "9780321486813"),
        ("The Pragmatic Programmer", "9780135957059"),
        ("Software Engineering: Pressman", "9780078022128")
    ]
    for i in range(10):
        catalog.append({
            "_id": oid(6, i+1),
            "institution_id": inst_id,
            "title": books[i][0],
            "isbn": books[i][1],
            "total_copies": 10,
            "available_copies": 7 + (i % 3)
        })
    seed("catalog_items", catalog)

    circulations = []
    for i in range(10):
        circulations.append({
            "_id": oid(6, 100 + i + 1),
            "institution_id": inst_id,
            "catalog_item_id": oid(6, i+1),
            "member_id": oid(12, (i % 4) + 1),
            "status": "issued" if i % 2 == 0 else "returned",
            "issued_at": datetime.now() - timedelta(days=i*2)
        })
    seed("book_circulations", circulations)

    digital_assets = []
    for i in range(10):
        digital_assets.append({
            "_id": oid(6, 200 + i + 1),
            "institution_id": inst_id,
            "title": f"IEEE Digital Research E-Journal Vol {i+1}"
        })
    seed("digital_library_assets", digital_assets)

    memberships = []
    for i in range(10):
        memberships.append({
            "_id": oid(6, 300 + i + 1),
            "institution_id": inst_id,
            "user_id": oid(12, i+1),
            "status": "active"
        })
    seed("memberships", memberships)

    fines = []
    for i in range(10):
        fines.append({
            "_id": oid(6, 400 + i + 1),
            "institution_id": inst_id,
            "member_id": oid(12, (i % 4) + 1),
            "amount": 20.0 + (i * 10),
            "status": "unpaid" if i % 2 == 0 else "paid"
        })
    seed("fines", fines)

    # =========================================================================
    # 8. HOSTEL MANAGEMENT
    # =========================================================================
    print("\n--- [8/13] Hostel Management ---")
    rooms = []
    for i in range(10):
        rooms.append({
            "_id": oid(7, i+1),
            "institution_id": inst_id,
            "hostel_name": "Tagore Hostel" if i < 5 else "Nehru Hostel",
            "room_number": f"{101+i}",
            "capacity": 2,
            "occupied_beds": 1
        })
    seed("rooms", rooms)

    allocations = []
    for i in range(10):
        allocations.append({
            "_id": oid(7, 100 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "room_id": oid(7, i+1),
            "academic_year": "2026-2027",
            "status": "active"
        })
    seed("room_allocations", allocations)

    checkins = []
    for i in range(10):
        checkins.append({
            "_id": oid(7, 200 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "direction": "in" if i % 2 == 0 else "out",
            "recorded_at": datetime.now() - timedelta(hours=i*2)
        })
    seed("hostel_checkins", checkins)

    mess_plans = []
    for i in range(10):
        mess_plans.append({
            "_id": oid(7, 300 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "plan_type": "veg" if i % 2 == 0 else "non_veg",
            "status": "active"
        })
    seed("mess_plans", mess_plans)

    visitors = []
    for i in range(10):
        visitors.append({
            "_id": oid(7, 400 + i + 1),
            "institution_id": inst_id,
            "visitor_name": f"Parent/Relative {i+1}",
            "student_id": oid(12, (i % 4) + 1)
        })
    seed("visitor_logs", visitors)

    maint_reqs = []
    issues = ["Fan Repair", "Plumbing Leak", "Light Switch Broken", "Window Glass Repair", "Door Lock Jammed", "AC Filter Clean", "Study Table Fix", "Water Heater Check", "WiFi Router Reset", "Paint Touchup"]
    for i in range(10):
        maint_reqs.append({
            "_id": oid(7, 500 + i + 1),
            "institution_id": inst_id,
            "room_id": oid(7, i+1),
            "issue": issues[i],
            "status": "open" if i % 2 == 0 else "resolved"
        })
    seed("maintenance_requests", maint_reqs)

    # =========================================================================
    # 9. PLACEMENT MANAGEMENT
    # =========================================================================
    print("\n--- [9/13] Placement Management ---")
    companies = []
    comp_names = ["Google", "Microsoft", "Amazon", "Apple", "Meta", "TCS", "Infosys", "Wipro", "Accenture", "IBM"]
    for i in range(10):
        companies.append({
            "_id": oid(8, i+1),
            "institution_id": inst_id,
            "name": comp_names[i],
            "industry": "Technology"
        })
    seed("companies", companies)

    postings = []
    titles = ["Software Development Engineer", "Data Scientist", "Cloud Architect", "Frontend Developer", "Backend Developer", "DevOps Engineer", "Product Manager", "QA Automation Engineer", "Cybersecurity Specialist", "AI Research Scientist"]
    for i in range(10):
        postings.append({
            "_id": oid(8, 100 + i + 1),
            "institution_id": inst_id,
            "company_id": oid(8, i+1),
            "title": titles[i],
            "status": "open"
        })
    seed("job_postings", postings)

    drives = []
    for i in range(10):
        drives.append({
            "_id": oid(8, 200 + i + 1),
            "institution_id": inst_id,
            "name": f"Campus Recruitment Drive Phase {i+1}"
        })
    seed("placement_drives", drives)

    applications = []
    app_statuses = ["applied", "shortlisted", "interviewing", "offered", "applied", "shortlisted", "interviewing", "offered", "applied", "rejected"]
    for i in range(10):
        applications.append({
            "_id": oid(8, 300 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "job_posting_id": oid(8, 100 + i + 1),
            "status": app_statuses[i]
        })
    seed("student_applications", applications)

    interviews = []
    for i in range(10):
        interviews.append({
            "_id": oid(8, 400 + i + 1),
            "institution_id": inst_id,
            "student_id": oid(12, (i % 4) + 1),
            "round": f"Technical Round { (i%3) + 1 }"
        })
    seed("interview_schedules", interviews)

    offers = []
    for i in range(10):
        offers.append({
            "_id": oid(8, 500 + i + 1),
            "institution_id": inst_id,
            "student_application_id": oid(8, 300 + i + 1),
            "status": "extended" if i % 2 == 0 else "accepted"
        })
    seed("offers", offers)

    # =========================================================================
    # 10. TRANSPORT MANAGEMENT
    # =========================================================================
    print("\n--- [10/13] Transport Management ---")
    routes = []
    for i in range(10):
        routes.append({
            "_id": oid(9, i+1),
            "institution_id": inst_id,
            "route_code": f"R-{10+i}",
            "name": f"Campus Route {i+1} Express"
        })
    seed("routes", routes)

    vehicles = []
    for i in range(10):
        vehicles.append({
            "_id": oid(9, 100 + i + 1),
            "institution_id": inst_id,
            "registration_number": f"KA-01-EQ-{1000+i}",
            "type": "bus",
            "capacity": 50
        })
    seed("vehicles", vehicles)

    v_assigns = []
    for i in range(10):
        v_assigns.append({
            "_id": oid(9, 200 + i + 1),
            "institution_id": inst_id,
            "vehicle_id": oid(9, 100 + i + 1),
            "route_id": oid(9, i+1),
            "driver_id": oid(12, 8) # Staff 1
        })
    seed("vehicle_assignments", v_assigns)

    drivers = []
    for i in range(10):
        drivers.append({
            "_id": oid(9, 300 + i + 1),
            "institution_id": inst_id,
            "driver_name": f"Driver {names[i]}",
            "license_number": f"DL-9988776{i}"
        })
    seed("driver_profiles", drivers)

    gps_pings = []
    for i in range(10):
        gps_pings.append({
            "ts": datetime.now() - timedelta(minutes=i*5),
            "meta": {"institution_id": inst_id, "vehicle_id": oid(9, 100 + (i%5) + 1)},
            "location": {"type": "Point", "coordinates": [77.5946 + (i*0.001), 12.9716 + (i*0.001)]},
            "speed_kmph": 30.0 + i
        })
    seed("gps_pings", gps_pings)

    # =========================================================================
    # 11. FACILITIES MANAGEMENT
    # =========================================================================
    print("\n--- [11/13] Facilities Management ---")
    assets = []
    asset_names = ["Server Rack 1", "Auditorium Projector", "Library Workstations", "Lab 1 Oscilloscopes", "Gymnasium Treadmills", "Main DG Generator", "Solar Panel Grid 1", "Campus Core Router", "HVAC Chiller Plant", "Cafeteria Refrigerator"]
    for i in range(10):
        assets.append({
            "_id": oid(10, i+1),
            "institution_id": inst_id,
            "asset_tag": f"AST-{2000+i}",
            "name": asset_names[i]
        })
    seed("assets", assets)

    work_orders = []
    for i in range(10):
        work_orders.append({
            "_id": oid(10, 100 + i + 1),
            "institution_id": inst_id,
            "asset_id": oid(10, i+1),
            "priority": "high" if i % 3 == 0 else "medium",
            "status": "assigned" if i % 2 == 0 else "resolved"
        })
    seed("work_orders", work_orders)

    inventory = []
    for i in range(10):
        inventory.append({
            "_id": oid(10, 200 + i + 1),
            "institution_id": inst_id,
            "sku": f"SKU-ITEM-{300+i}",
            "name": f"Spare Item {i+1}",
            "quantity_on_hand": 100 * (i + 1)
        })
    seed("inventory_items", inventory)

    vendors = []
    for i in range(10):
        vendors.append({
            "_id": oid(10, 300 + i + 1),
            "institution_id": inst_id,
            "vendor_name": f"Vendor Enterprise {i+1}"
        })
    seed("vendors", vendors)

    utility_readings = []
    for i in range(10):
        utility_readings.append({
            "_id": oid(10, 400 + i + 1),
            "institution_id": inst_id,
            "meter_type": "ELECTRICITY" if i%2==0 else "WATER",
            "reading_value": 1000.0 + (i * 250.5)
        })
    seed("utility_readings", utility_readings)

    prev_maint = []
    for i in range(10):
        prev_maint.append({
            "_id": oid(10, 500 + i + 1),
            "institution_id": inst_id,
            "asset_id": oid(10, i+1),
            "frequency": "MONTHLY" if i%2==0 else "QUARTERLY"
        })
    seed("preventive_maintenance_schedules", prev_maint)

    # =========================================================================
    # 12. ALUMNI MANAGEMENT
    # =========================================================================
    print("\n--- [12/13] Alumni Management ---")
    alumni_profs = []
    employers = ["Google", "Microsoft", "Amazon", "Apple", "Meta", "Netflix", "Tesla", "Adobe", "Intel", "Oracle"]
    for i in range(10):
        alumni_profs.append({
            "_id": oid(11, i+1),
            "institution_id": inst_id,
            "user_id": oid(12, i+1),
            "graduation_year": 2020 + (i % 5),
            "current_employer": employers[i]
        })
    seed("alumni_profiles", alumni_profs)

    alumni_events = []
    for i in range(10):
        alumni_events.append({
            "_id": oid(11, 100 + i + 1),
            "institution_id": inst_id,
            "event_name": f"Alumni Meet Edition {i+1}"
        })
    seed("alumni_events", alumni_events)

    mentorship = []
    for i in range(10):
        mentorship.append({
            "_id": oid(11, 200 + i + 1),
            "institution_id": inst_id,
            "alumni_profile_id": oid(11, i+1),
            "student_id": oid(12, (i % 4) + 1)
        })
    seed("mentorship_pairs", mentorship)

    donations = []
    for i in range(10):
        donations.append({
            "_id": oid(11, 300 + i + 1),
            "institution_id": inst_id,
            "alumni_profile_id": oid(11, i+1),
            "amount": 5000.0 + (i * 2000),
            "idempotency_key": f"DON-KEY-{500+i}"
        })
    seed("donations", donations)

    scores = []
    for i in range(10):
        scores.append({
            "_id": oid(11, 400 + i + 1),
            "institution_id": inst_id,
            "alumni_profile_id": oid(11, i+1),
            "score": 75.0 + (i * 2.5)
        })
    seed("engagement_scores", scores)

    # =========================================================================
    # 13. COMMUNICATION & ANNOUNCEMENT
    # =========================================================================
    print("\n--- [13/13] Communication & Announcement ---")
    announcements = []
    for i in range(10):
        announcements.append({
            "_id": oid(13, i+1),
            "institution_id": inst_id,
            "title": f"Official Announcement {i+1}: Important Updates for Students and Staff",
            "published_at": datetime.now() - timedelta(days=i)
        })
    seed("announcements", announcements)

    campaigns = []
    for i in range(10):
        campaigns.append({
            "_id": oid(13, 100 + i + 1),
            "institution_id": inst_id,
            "name": f"Outbound Communication Campaign {i+1}",
            "status": "running" if i%2==0 else "completed"
        })
    seed("campaigns", campaigns)

    notif_logs = []
    channels = ["push", "email", "sms", "whatsapp", "in_app", "push", "email", "sms", "whatsapp", "in_app"]
    for i in range(10):
        notif_logs.append({
            "_id": oid(13, 200 + i + 1),
            "institution_id": inst_id,
            "recipient_id": oid(12, (i % 4) + 1),
            "channel": channels[i],
            "status": "delivered",
            "sent_at": datetime.now() - timedelta(hours=i)
        })
    seed("notification_logs", notif_logs)

    surveys = []
    for i in range(10):
        surveys.append({
            "_id": oid(13, 300 + i + 1),
            "institution_id": inst_id,
            "survey_id": oid(13, 300 + i + 1),
            "user_id": oid(12, (i % 4) + 1)
        })
    seed("survey_responses", surveys)

    alerts = []
    severities = ["advisory", "warning", "critical", "advisory", "warning", "critical", "advisory", "warning", "critical", "warning"]
    for i in range(10):
        alerts.append({
            "_id": oid(13, 400 + i + 1),
            "institution_id": inst_id,
            "message": f"Campus Emergency Alert {i+1}: Please take note of administrative safety instructions.",
            "severity": severities[i],
            "broadcast_at": datetime.now() - timedelta(hours=i*12)
        })
    seed("emergency_alerts", alerts)

    print("\n============================================================================")
    print(" 10-Records-Per-Collection Seeding Completed Successfully Across All Modules!")
    print("============================================================================")

if __name__ == "__main__":
    main()
