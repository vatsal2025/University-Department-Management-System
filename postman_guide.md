# Postman Guide for UDIIMS

## 1. Purpose of This Guide

This guide explains how to create Postman requests for this project:

- how to know what goes in the URL
- how to know what goes in query params
- how to know what goes in the JSON body
- which HTTP method to use
- which headers to set
- examples for the actual UDIIMS endpoints

This project uses a Spring Boot backend running at:

```text
http://localhost:8080
```

Most API routes begin with one of these prefixes:

- `/api/auth`
- `/api/students`
- `/api/secretary`
- `/api/finance`
- `/api/departments`

## 2. Before You Start

Make sure the backend is running:

```bash
cd backend
./mvnw spring-boot:run
```

Then open Postman.

## 3. Recommended Postman Setup

### Create an environment

In Postman, create an environment called something like `UDIIMS Local`.

Add these variables:

| Variable | Example Value |
|---|---|
| `baseUrl` | `http://localhost:8080` |
| `studentId` | `S001` |
| `departmentId` | `CSE` |
| `semesterTerm` | `Sem-1-2025` |
| `courseCode` | `CS101` |
| `facultyId` | `F001` |
| `projectId` | `PRJ101` |
| `itemId` | `INV101` |

Then in requests, you can use:

```text
{{baseUrl}}/api/students/{{studentId}}
```

instead of typing everything manually every time.

## 4. The 4 Things You Must Decide for Every Request

For every API request, you need to decide:

1. HTTP method
2. URL
3. Query parameters or path variables
4. Request body

### 4.1 HTTP method

Use the method based on what you want to do:

| Method | Meaning | Typical Use |
|---|---|---|
| `GET` | Read data | fetch student, list projects, list grants |
| `POST` | Create data or trigger action | login, add faculty, register courses |
| `PUT` | Update an existing item | update faculty, update project |
| `PATCH` | Partially update data | update fee status |
| `DELETE` | Remove or deactivate something | drop course, dispose item |

### 4.2 URL

A URL usually has 3 parts:

```text
{{baseUrl}} + endpoint path + optional query parameters
```

Example:

```text
{{baseUrl}}/api/students/{{studentId}}/registrations?semesterTerm={{semesterTerm}}
```

## 5. How to Know What Goes Where

This is the most important part.

### 5.1 Path variable: goes inside the URL path

If the controller uses `@PathVariable`, the value must go inside the URL itself.

Example from the backend:

```java
@GetMapping("/{studentId}")
```

This means `studentId` goes in the path:

```text
/api/students/S001
```

Not in the body.
Not in query params.

Another example:

```java
@DeleteMapping("/{studentId}/registrations/{courseCode}")
```

This means both values go in the URL:

```text
/api/students/S001/registrations/CS101
```

### 5.2 Query parameter: goes in Params tab

If the controller uses `@RequestParam`, put it in Postman `Params`.

Example:

```java
@GetMapping("/courses/available")
public ResponseEntity<?> getAvailableCourses(
    @RequestParam String semesterTerm,
    @RequestParam(required = false) String departmentId)
```

So:

- `semesterTerm` goes in Params
- `departmentId` goes in Params

Final request:

```text
GET {{baseUrl}}/api/students/courses/available
```

Params:

| Key | Value |
|---|---|
| `semesterTerm` | `Sem-1-2025` |
| `departmentId` | `CSE` |

### 5.3 JSON body: goes in Body tab

If the controller uses `@RequestBody`, send JSON in the Body tab.

In Postman:

- open `Body`
- choose `raw`
- choose `JSON`

Example:

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> body)
```

This means the request needs a JSON body like:

```json
{
  "role": "student",
  "id": "S001",
  "password": "pass123"
}
```

### 5.4 Sometimes a request uses both URL params and JSON body

This project has several endpoints where some data is in the URL and some in the body.

Example:

```java
@PostMapping("/{studentId}/registrations")
```

Path variable:

- `studentId` goes in URL

Body:

- `semester_term`
- `course_codes`

So the request becomes:

```text
POST {{baseUrl}}/api/students/S001/registrations
```

Body:

```json
{
  "semester_term": "Sem-1-2025",
  "course_codes": ["CS101", "CS102"]
}
```

## 6. Headers

For most `POST`, `PUT`, and `PATCH` requests in this project, use:

| Key | Value |
|---|---|
| `Content-Type` | `application/json` |

In Postman, this is often set automatically if you choose `Body -> raw -> JSON`.

For `GET` and `DELETE`, you usually do not need to manually set this unless you want to be explicit.

## 7. Reading Spring Controller Code to Build Requests

This is the easiest rule:

- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping` tells you the method and route
- `@PathVariable` means value goes in URL path
- `@RequestParam` means value goes in query params
- `@RequestBody` means value goes in JSON body

Example:

```java
@PatchMapping("/fees/{studentId}")
public ResponseEntity<?> updateFeeStatus(
    @PathVariable String studentId,
    @RequestBody Map<String, String> body)
```

From this, you know:

- method = `PATCH`
- URL contains `studentId`
- request body contains the rest

So:

```text
PATCH {{baseUrl}}/api/finance/fees/S001
```

Body:

```json
{
  "semester_term": "Sem-1-2025",
  "fee_status": "paid"
}
```

## 8. Common Request Patterns in This Project

## 8.1 Login

### Endpoint

```text
POST /api/auth/login
```

### What goes where

- URL path: nothing dynamic
- query params: none
- body: `role`, `id`, `password`

### Postman setup

Method:

```text
POST
```

URL:

```text
{{baseUrl}}/api/auth/login
```

Body:

```json
{
  "role": "student",
  "id": "S001",
  "password": "pass123"
}
```

### Notes

- student IDs must match the format `S` followed by numbers, like `S001`
- invalid ID returns `Invalid ID`
- wrong password returns `Invalid password`

## 8.2 Get student profile

### Endpoint

```text
GET /api/students/{studentId}
```

### Postman setup

URL:

```text
{{baseUrl}}/api/students/{{studentId}}
```

No body.

## 8.3 Get available courses

### Endpoint

```text
GET /api/students/courses/available
```

### Postman setup

URL:

```text
{{baseUrl}}/api/students/courses/available
```

Params:

| Key | Value |
|---|---|
| `semesterTerm` | `{{semesterTerm}}` |
| `departmentId` | `{{departmentId}}` |

No body.

## 8.4 Register courses

### Endpoint

```text
POST /api/students/{studentId}/registrations
```

### Postman setup

URL:

```text
{{baseUrl}}/api/students/{{studentId}}/registrations
```

Body:

```json
{
  "semester_term": "Sem-1-2025",
  "course_codes": ["CS101", "CS102"]
}
```

### Rule

- `studentId` is in URL because it is a path variable
- `semester_term` and `course_codes` are in body because they belong to `@RequestBody`

## 8.5 Drop a course

### Endpoint

```text
DELETE /api/students/{studentId}/registrations/{courseCode}
```

### Postman setup

URL:

```text
{{baseUrl}}/api/students/{{studentId}}/registrations/{{courseCode}}
```

Params:

| Key | Value |
|---|---|
| `semesterTerm` | `{{semesterTerm}}` |

No body.

## 8.6 Add faculty

### Endpoint

```text
POST /api/secretary/faculty
```

### Postman setup

URL:

```text
{{baseUrl}}/api/secretary/faculty
```

Body:

```json
{
  "faculty_id": "F101",
  "faculty_name": "Dr. Ramesh Kumar",
  "designation": "Professor",
  "department_name": "Computer Science and Engineering",
  "specialization": "Machine Learning",
  "department_id": "CSE",
  "active_status": true
}
```

### Notes

- `faculty_id` must start with `F` and then numbers only
- example valid values: `F1`, `F101`
- example invalid values: `f101`, `FAC101`

## 8.7 Update faculty

### Endpoint

```text
PUT /api/secretary/faculty/{facultyId}
```

### Postman setup

URL:

```text
{{baseUrl}}/api/secretary/faculty/{{facultyId}}
```

Body example:

```json
{
  "faculty_name": "Dr. Ramesh Kumar",
  "designation": "Senior Professor",
  "specialization": "Artificial Intelligence"
}
```

### Notes

- usually the ID in the path tells the server which faculty record to update
- if you include `faculty_id` in the JSON body, it must also follow the format rule

## 8.8 Add project

### Endpoint

```text
POST /api/secretary/projects
```

### Postman setup

URL:

```text
{{baseUrl}}/api/secretary/projects
```

Body:

```json
{
  "project_id": "PRJ101",
  "project_title": "Smart Attendance System",
  "faculty_id": "F101",
  "project_budget": 50000,
  "project_status": "active",
  "abstract": "AI-based attendance project",
  "publication_link": "",
  "department_id": "CSE"
}
```

### Notes

- `project_id` must start with `PRJ` and then numbers only
- `faculty_id` must refer to an existing active faculty member if provided

## 8.9 Add inventory item

### Endpoint

```text
POST /api/secretary/inventory
```

### Important

This endpoint uses both query params and body.

### Postman setup

URL:

```text
{{baseUrl}}/api/secretary/inventory
```

Params:

| Key | Value |
|---|---|
| `departmentId` | `CSE` |
| `isTechnical` | `true` |

Body:

```json
{
  "item_id": "INV101",
  "item_name": "Oscilloscope",
  "category": "Electronics",
  "quantity": 2,
  "location": "Lab 2",
  "condition": "new",
  "is_lab_item": true,
  "lab_incharge_id": "F101"
}
```

### Notes

- `item_id` must start with `INV` and then numbers only
- `departmentId` is in Params because the controller uses `@RequestParam`
- inventory data itself goes in body because it comes from `@RequestBody`

## 8.10 Get department accounts

### Endpoint

```text
GET /api/secretary/accounts
```

### Postman setup

URL:

```text
{{baseUrl}}/api/secretary/accounts
```

Params:

| Key | Value |
|---|---|
| `departmentId` | `CSE` |

No body.

## 8.11 Add course offering

### Endpoint

```text
POST /api/secretary/courses
```

### Postman setup

Body:

```json
{
  "course_code": "CS301",
  "course_name": "Data Mining",
  "credit_hours": 4,
  "faculty_id": "F101",
  "semester_term": "Sem-1-2025",
  "department_id": "CSE"
}
```

## 8.12 Update fee status

### Endpoint

```text
PATCH /api/finance/fees/{studentId}
```

### Postman setup

URL:

```text
{{baseUrl}}/api/finance/fees/{{studentId}}
```

Body:

```json
{
  "semester_term": "Sem-1-2025",
  "fee_status": "paid"
}
```

## 8.13 Create fee record

### Endpoint

```text
POST /api/finance/fees
```

Body:

```json
{
  "student_id": "S001",
  "semester_term": "Sem-1-2025",
  "amount": 50000,
  "fee_status": "pending",
  "department_id": "CSE"
}
```

## 8.14 Record a fee payment

### Endpoint

```text
POST /api/finance/fees/{studentId}/payments
```

Body:

```json
{
  "semester_term": "Sem-1-2025",
  "amount": 25000,
  "payment_method": "online",
  "payment_date": "2026-04-17T10:00:00Z",
  "notes": "First installment"
}
```

## 8.15 Record grant

### Endpoint

```text
POST /api/finance/grants
```

Body:

```json
{
  "grant_id": "GRANT-CSE-001",
  "amount": 100000,
  "transaction_date": "2026-04-17T10:00:00Z",
  "department_id": "CSE"
}
```

## 9. How to Build a Postman Request from Scratch

Use this checklist every time:

### Step 1: Find the controller method

Example:

```java
@PostMapping("/projects")
public ResponseEntity<?> addProject(@RequestBody Map<String, Object> body)
```

From this you learn:

- method = `POST`
- route = `/api/secretary/projects`
- data goes in body

### Step 2: Look for `@PathVariable`

If present, those values go in the URL path.

Example:

```java
@GetMapping("/fees/{studentId}")
```

means:

```text
/api/finance/fees/S001
```

### Step 3: Look for `@RequestParam`

If present, those go in Postman Params.

Example:

```java
@GetMapping("/grants")
public ResponseEntity<?> getGrants(@RequestParam(required = false) String departmentId)
```

means:

- URL is `/api/finance/grants`
- `departmentId` goes in Params

### Step 4: Look for `@RequestBody`

If present, send JSON.

### Step 5: Check service validations

Some requests may fail if IDs or values are invalid.

Examples in this project:

- student login ID must be like `S001`
- faculty ID must be like `F101`
- project ID must be like `PRJ101`
- inventory ID must be like `INV101`

## 10. Common Mistakes and How to Avoid Them

### Mistake 1: Putting path variables in the body

Wrong:

```json
{
  "studentId": "S001"
}
```

for:

```text
GET /api/students/{studentId}
```

Correct:

```text
GET /api/students/S001
```

### Mistake 2: Putting query params in the body

If the controller uses `@RequestParam`, use the Params tab, not raw JSON.

### Mistake 3: Using wrong HTTP method

Examples:

- use `GET` for fetching
- use `POST` for creating
- use `PUT` or `PATCH` for updating
- use `DELETE` for removing

### Mistake 4: Forgetting JSON header

For body-based requests, use:

```text
Content-Type: application/json
```

### Mistake 5: Sending invalid IDs

Examples of valid IDs:

- student: `S001`
- faculty: `F101`
- project: `PRJ101`
- inventory: `INV101`

## 11. Quick Reference Table

| Endpoint Type | URL Path | Params | Body |
|---|---|---|---|
| Login | fixed | none | yes |
| Get student | has `studentId` | none | no |
| Get available courses | fixed | yes | no |
| Register courses | has `studentId` | none | yes |
| Drop course | has `studentId`, `courseCode` | yes | no |
| Add faculty | fixed | none | yes |
| Update faculty | has `facultyId` | none | yes |
| Add project | fixed | none | yes |
| Add inventory | fixed | yes | yes |
| Update fee status | has `studentId` | none | yes |

## 12. Best Way to Learn Faster

If you are unsure about any request:

1. open the controller method
2. identify the HTTP method
3. identify the mapping path
4. check `@PathVariable`
5. check `@RequestParam`
6. check `@RequestBody`
7. build the request in Postman accordingly

For this project, you can also use:

- [README.md](/Users/ritwikbhattacharyya/SWE_proj/University-Department-Management-System/README.md)
- [UniversityERP_Tests.postman_collection.json](/Users/ritwikbhattacharyya/SWE_proj/University-Department-Management-System/postman/UniversityERP_Tests.postman_collection.json)

## 13. Final Tip

When you are confused, ask yourself these 3 questions:

1. Is this value part of the resource identity?
If yes, it usually goes in the URL path.

2. Is this just a filter or option?
If yes, it usually goes in query params.

3. Is this the actual data I want to create or update?
If yes, it usually goes in the JSON body.

That one rule will help you build almost every request in this project correctly.
