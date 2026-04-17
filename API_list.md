# Backend API Endpoints

This document outlines the API endpoints available in the University Department Management System backend.

## Auth Controller (`/api/auth`)
* `POST /api/auth/login` - User authentication login

## Department Fund Controller (`/api/departments`)
* `GET /api/departments/{departmentId}/fund-sources` - Get fund sources for a department
* `POST /api/departments/{departmentId}/fund-sources` - Add a fund source for a department
* `GET /api/departments/{departmentId}/fund-usage` - Get fund usages for a department
* `POST /api/departments/{departmentId}/fund-usage` - Add a fund usage for a department
* `GET /api/departments/{departmentId}/fund-summary` - Get the overall fund summary for a department

## Finance Controller (`/api/finance`)
* `GET /api/finance/grants` - Retrieve list of grants
* `POST /api/finance/grants` - Add a new grant
* `GET /api/finance/consultancy` - Retrieve list of consultancy projects
* `POST /api/finance/consultancy` - Add new consultancy details
* `GET /api/finance/fees/{studentId}` - Get fee details for a specific student
* `PATCH /api/finance/fees/{studentId}` - Update fee details for a specific student
* `POST /api/finance/fees` - Add overall fee entry
* `POST /api/finance/fees/{studentId}/payments` - Log a fee payment for a specific student
* `GET /api/finance/fees/{studentId}/payments` - Retrieve fee payments for a specific student
* `GET /api/finance/projects` - Get overview of projects (Finance perspective)
* `GET /api/finance/project-finance` - Retrieve list of project finances
* `POST /api/finance/project-finance` - Add new project finance details

## Secretary Controller (`/api/secretary`)
* `GET /api/secretary/faculty` - Retrieve list of all faculty members
* `POST /api/secretary/faculty` - Add a new faculty member
* `PUT /api/secretary/faculty/{facultyId}` - Update an existing faculty member
* `DELETE /api/secretary/faculty/{facultyId}` - Remove a faculty member
* `DELETE /api/secretary/faculty/{facultyId}/force` - Force remove a faculty member
* `GET /api/secretary/projects` - Retrieve list of all projects
* `POST /api/secretary/projects` - Add a new project
* `PUT /api/secretary/projects/{projectId}` - Update an existing project
* `GET /api/secretary/inventory` - Retrieve inventory list
* `POST /api/secretary/inventory` - Add an item to the inventory
* `PUT /api/secretary/inventory/{itemId}` - Update an inventory item
* `DELETE /api/secretary/inventory/{itemId}` - Delete an item from the inventory
* `GET /api/secretary/accounts` - Retrieve account details
* `GET /api/secretary/courses` - Retrieve list of courses
* `POST /api/secretary/courses` - Add a new course
* `PUT /api/secretary/courses/{courseCode}` - Update details for a specific course
* `DELETE /api/secretary/courses/{courseCode}` - Remove a course
* `GET /api/secretary/departments/{departmentId}` - Retrieve department details

## Student Controller (`/api/students`)
* `GET /api/students/{studentId}` - Retrieve student details 
* `GET /api/students/courses/available` - Get available courses for students
* `GET /api/students/{studentId}/registrations` - Get all course registrations for a student
* `POST /api/students/{studentId}/registrations` - Register a student for a course
* `DELETE /api/students/{studentId}/registrations/{courseCode}` - Remove a course registration for a student
* `POST /api/students/{studentId}/gpa/calculate` - Trigger GPA calculation for a student
* `GET /api/students/{studentId}/gpa` - Retrieve the computed GPA for a student
* `GET /api/students/{studentId}/backlogs` - Get backlog courses for a student
* `GET /api/students/{studentId}/fees` - Retrieve fee details for a student
