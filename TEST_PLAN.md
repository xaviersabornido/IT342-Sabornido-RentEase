# RentEase - Comprehensive Software Test Plan

**Version:** 1.0  
**Date:** April 27, 2026  
**Project:** RentEase Rental Platform  
**Scope:** Full system testing (Backend, Web Frontend, Mobile)  

---

## 1. Test Strategy Overview

### 1.1 Objectives
- Validate all implemented functional requirements
- Ensure system stability and data integrity
- Identify bugs and regressions after refactoring
- Verify user workflows end-to-end
- Establish baseline for future development

### 1.2 Test Scope
**In Scope:**
- Phase 1 Auth features (all platforms)
- Backend Listings, Requests, Ratings APIs
- Web Frontend UI/UX (to implemented state)
- Mobile Auth (Phase 1)
- Database constraints and triggers
- Security (JWT, role-based access)

**Out of Scope:**
- Phase 2+ features (not implemented)
- Performance/load testing (beyond basic response times)
- Security penetration testing

### 1.3 Test Levels
1. **Unit Tests** - Service/Repository logic
2. **Integration Tests** - Controller + Service + Database
3. **API Tests** - REST endpoint validation
4. **UI Tests** - Web frontend workflows
5. **Smoke Tests** - End-to-end critical paths

---

## 2. Functional Requirements Coverage Matrix

| Requirement | Backend | Web | Mobile | Test Type | Status |
|---|---|---|---|---|---|
| **AUTH: User Registration** | ✅ | ✅ | ✅ | Integration | Planned |
| **AUTH: User Login** | ✅ | ✅ | ✅ | Integration | Planned |
| **AUTH: JWT Token Management** | ✅ | N/A | ✅ | Unit | Planned |
| **AUTH: Role-based Access** | ✅ | ✅ | ✅ | Integration | Planned |
| **LISTINGS: Create Listing** | ✅ | ✅ | N/A | Integration | Planned |
| **LISTINGS: Get All Listings** | ✅ | ✅ | N/A | Integration | Planned |
| **LISTINGS: Get Listing Details** | ✅ | ✅ | N/A | Integration | Planned |
| **LISTINGS: Update Listing** | ✅ | ✅ | N/A | Integration | Planned |
| **LISTINGS: Delete Listing** | ✅ | ✅ | N/A | Integration | Planned |
| **REQUESTS: Create Rental Request** | ✅ | ✅ | N/A | Integration | Planned |
| **REQUESTS: Get Requests** | ✅ | ✅ | N/A | Integration | Planned |
| **REQUESTS: Approve/Decline Request** | ✅ | ✅ | N/A | Integration | Planned |
| **RATINGS: Create Rating** | ✅ | ✅ | N/A | Integration | Planned |
| **RATINGS: Get Ratings** | ✅ | ✅ | N/A | Integration | Planned |

---

## 3. Test Cases by Feature

### 3.1 AUTHENTICATION TESTS

#### TC-AUTH-001: User Registration - Happy Path
**Purpose:** Verify new user can register successfully  
**Prerequisites:** Backend is running, database is clean  
**Steps:**
1. Send POST to `/api/v1/auth/register` with valid data:
   ```json
   {
     "email": "testuser@example.com",
     "password": "SecurePass123!",
     "firstName": "Test",
     "lastName": "User",
     "role": "RENTER"
   }
   ```
2. Verify response code is 200/201
3. Verify response contains user object with ID, email, firstName, lastName, role
4. Verify user is stored in database with hashed password
5. Query database to confirm user exists and role is RENTER

**Expected Result:** User registered successfully, password hashed, appears in database

---

#### TC-AUTH-002: User Registration - Duplicate Email
**Purpose:** Verify system prevents duplicate email registration  
**Prerequisites:** User with email "duplicate@example.com" exists  
**Steps:**
1. Attempt to register new user with existing email "duplicate@example.com"
2. Verify response code is 400 (Bad Request)
3. Verify error message indicates email already exists

**Expected Result:** Registration rejected with appropriate error message

---

#### TC-AUTH-003: User Registration - Invalid Email Format
**Purpose:** Verify email validation  
**Prerequisites:** Backend is running  
**Steps:**
1. Send registration request with invalid email: "notanemail"
2. Verify response code is 400
3. Verify error indicates invalid email format

**Expected Result:** Registration rejected, email validation works

---

#### TC-AUTH-004: User Login - Happy Path
**Purpose:** Verify user login and JWT token generation  
**Prerequisites:** User "testuser@example.com" exists with password "SecurePass123!"  
**Steps:**
1. Send POST to `/api/v1/auth/login` with credentials:
   ```json
   {
     "email": "testuser@example.com",
     "password": "SecurePass123!"
   }
   ```
2. Verify response code is 200
3. Verify response contains JWT accessToken and refreshToken
4. Decode JWT and verify it contains userId, email, role claims
5. Verify JWT expires in appropriate time (check expiration claim)

**Expected Result:** Login successful, valid JWT tokens returned

---

#### TC-AUTH-005: User Login - Invalid Credentials
**Purpose:** Verify login fails with wrong credentials  
**Prerequisites:** User exists  
**Steps:**
1. Send login request with correct email but wrong password
2. Verify response code is 401 (Unauthorized)
3. Verify error message indicates invalid credentials
4. No tokens should be returned

**Expected Result:** Login rejected, no tokens issued

---

#### TC-AUTH-006: JWT Token Validation
**Purpose:** Verify JWT tokens are validated on protected endpoints  
**Prerequisites:** Valid token and invalid token strings available  
**Steps:**
1. Try to access protected endpoint (e.g., GET `/api/v1/listings`) without token
2. Verify response code is 401
3. Try with expired token
4. Verify response code is 401
5. Try with valid token
6. Verify access is granted (200)

**Expected Result:** Only valid tokens grant access to protected endpoints

---

#### TC-AUTH-007: Role-Based Access Control
**Purpose:** Verify owners and renters have appropriate access  
**Prerequisites:** Owner account exists, Renter account exists  
**Steps:**
1. Login as RENTER
2. Attempt to access owner-only endpoint (if any are secured)
3. Verify access is denied (403)
4. Login as OWNER
5. Verify access is granted

**Expected Result:** Role-based restrictions enforced

---

### 3.2 LISTINGS TESTS

#### TC-LISTING-001: Create Listing - Happy Path
**Purpose:** Verify owner can create property listing  
**Prerequisites:** Owner is authenticated, JWT token available  
**Steps:**
1. Send POST to `/api/v1/listings` with valid data:
   ```json
   {
     "title": "Cozy 2BR Apartment",
     "description": "Furnished apartment near downtown",
     "price": 1500.00,
     "location": "123 Main St, Downtown",
     "bedrooms": 2,
     "bathrooms": 1,
     "status": "AVAILABLE"
   }
   ```
2. Include valid JWT token in Authorization header
3. Verify response code is 201 (Created)
4. Verify response contains listing ID, title, price, location, status
5. Query database to confirm listing exists with owner_id matching authenticated user

**Expected Result:** Listing created successfully and persisted

---

#### TC-LISTING-002: Create Listing - Missing Required Fields
**Purpose:** Verify validation for required fields  
**Prerequisites:** Owner is authenticated  
**Steps:**
1. Send POST without "title" field
2. Verify response code is 400
3. Verify error message indicates "title is required"

**Expected Result:** Validation error returned

---

#### TC-LISTING-003: Get All Listings
**Purpose:** Verify retrieval of all available listings  
**Prerequisites:** At least 3 listings exist in database  
**Steps:**
1. Send GET to `/api/v1/listings`
2. Verify response code is 200
3. Verify response contains array of listings
4. Verify count matches or exceeds number of listings in database
5. Verify each listing contains required fields (id, title, price, owner, status)

**Expected Result:** All listings returned with complete data

---

#### TC-LISTING-004: Get Listing by ID
**Purpose:** Verify detailed listing retrieval  
**Prerequisites:** Listing with ID "123" exists  
**Steps:**
1. Send GET to `/api/v1/listings/123`
2. Verify response code is 200
3. Verify response contains all listing details
4. Verify data matches database record

**Expected Result:** Correct listing details returned

---

#### TC-LISTING-005: Get Listing - Non-existent ID
**Purpose:** Verify error handling for missing listing  
**Prerequisites:** Listing ID "99999" doesn't exist  
**Steps:**
1. Send GET to `/api/v1/listings/99999`
2. Verify response code is 404 (Not Found)
3. Verify error message indicates listing not found

**Expected Result:** 404 error returned

---

#### TC-LISTING-006: Update Listing - Owner Permission
**Purpose:** Verify owner can update own listing  
**Prerequisites:** Owner has listing with ID "100", owner is authenticated  
**Steps:**
1. Send PUT to `/api/v1/listings/100` with updated data:
   ```json
   {
     "title": "Updated Title",
     "price": 1600.00,
     "status": "UNAVAILABLE"
   }
   ```
2. Include owner's JWT token
3. Verify response code is 200
4. Verify updated data is returned in response
5. Query database to confirm updates persisted

**Expected Result:** Listing updated successfully

---

#### TC-LISTING-007: Update Listing - Permission Denied
**Purpose:** Verify non-owner cannot update listing  
**Prerequisites:** Listing owned by User A, authenticated as User B  
**Steps:**
1. Authenticate as User B (renter)
2. Send PUT to update listing owned by User A
3. Verify response code is 403 (Forbidden)
4. Verify error message indicates permission denied
5. Verify listing was not modified in database

**Expected Result:** Update rejected, permission denied

---

#### TC-LISTING-008: Delete Listing - Happy Path
**Purpose:** Verify owner can delete own listing  
**Prerequisites:** Listing with ID "200" owned by authenticated user  
**Steps:**
1. Send DELETE to `/api/v1/listings/200` with valid JWT
2. Verify response code is 200 or 204
3. Try to GET the deleted listing
4. Verify response code is 404
5. Verify listing no longer in database

**Expected Result:** Listing deleted successfully

---

### 3.3 RENTAL REQUESTS TESTS

#### TC-REQUEST-001: Create Rental Request - Happy Path
**Purpose:** Verify renter can request to rent a property  
**Prerequisites:** Listing "100" exists, renter is authenticated  
**Steps:**
1. Send POST to `/api/v1/requests` with:
   ```json
   {
     "listingId": 100,
     "rentalStartDate": "2026-05-01",
     "rentalEndDate": "2026-05-31",
     "message": "Interested in this property"
   }
   ```
2. Include renter's JWT token
3. Verify response code is 201
4. Verify response contains request ID, status (should be PENDING)
5. Verify request stored in database with correct listing_id and renter_id

**Expected Result:** Request created with PENDING status

---

#### TC-REQUEST-002: Get Owner's Rental Requests
**Purpose:** Verify owner can view requests for their listings  
**Prerequisites:** Owner has 2+ requests for their listings, owner authenticated  
**Steps:**
1. Send GET to `/api/v1/requests` (or similar endpoint)
2. Include owner's JWT token
3. Verify response code is 200
4. Verify response contains all requests for owner's listings
5. Verify each request shows renter info, dates, message

**Expected Result:** Owner sees all requests for their properties

---

#### TC-REQUEST-003: Get Renter's Rental Requests
**Purpose:** Verify renter can view their own requests  
**Prerequisites:** Renter has made 2+ requests, renter authenticated  
**Steps:**
1. Send GET to `/api/v1/requests` with renter JWT token
2. Verify response contains only requests made by that renter
3. Verify request statuses are visible

**Expected Result:** Renter sees their own requests

---

#### TC-REQUEST-004: Approve Rental Request
**Purpose:** Verify owner can approve request  
**Prerequisites:** Request "50" exists with status PENDING, owner authenticated  
**Steps:**
1. Send PUT to `/api/v1/requests/50` with:
   ```json
   {
     "status": "APPROVED"
   }
   ```
2. Include owner's JWT token
3. Verify response code is 200
4. Verify status in response is APPROVED
5. Query database to confirm status updated

**Expected Result:** Request approved, status changed

---

#### TC-REQUEST-005: Decline Rental Request
**Purpose:** Verify owner can decline request  
**Prerequisites:** Request "51" exists with status PENDING, owner authenticated  
**Steps:**
1. Send PUT to `/api/v1/requests/51` with:
   ```json
   {
     "status": "DECLINED"
   }
   ```
2. Include owner's JWT token
3. Verify response code is 200
4. Verify status is DECLINED
5. Query database to confirm

**Expected Result:** Request declined, status changed

---

### 3.4 RATINGS TESTS

#### TC-RATING-001: Create Rating - Happy Path
**Purpose:** Verify renter can rate an owner  
**Prerequisites:** Renter has completed rental with owner, renter authenticated  
**Steps:**
1. Send POST to `/api/v1/ratings` with:
   ```json
   {
     "ownerId": 5,
     "rating": 5,
     "comment": "Great communication and clean property!"
   }
   ```
2. Include renter's JWT token
3. Verify response code is 201
4. Verify response contains rating ID, rating value, comment, owner ID
5. Verify rating persisted in database

**Expected Result:** Rating created successfully

---

#### TC-RATING-002: Rating Validation - Invalid Score
**Purpose:** Verify rating must be 1-5  
**Prerequisites:** Renter authenticated  
**Steps:**
1. Send POST with rating value: 10
2. Verify response code is 400
3. Verify error indicates rating must be 1-5

**Expected Result:** Invalid rating rejected

---

#### TC-RATING-003: Get Owner Ratings
**Purpose:** Verify ability to retrieve owner's ratings  
**Prerequisites:** Owner ID "5" has 3+ ratings  
**Steps:**
1. Send GET to `/api/v1/ratings?ownerId=5`
2. Verify response code is 200
3. Verify response contains all ratings for that owner
4. Verify average rating calculated correctly

**Expected Result:** Owner's ratings retrieved successfully

---

### 3.5 WEB FRONTEND TESTS

#### TC-WEB-001: User Login Flow
**Purpose:** Verify login page and token storage  
**Steps:**
1. Open web app at http://localhost:5173
2. Click "Login" button
3. Enter valid credentials
4. Verify redirected to dashboard
5. Check browser localStorage/sessionStorage for JWT token
6. Verify token used in subsequent requests

**Expected Result:** Login successful, token stored and used

---

#### TC-WEB-002: Create Listing Flow (Owner)
**Purpose:** Verify listing creation from web UI  
**Prerequisites:** Logged in as owner  
**Steps:**
1. Navigate to "List Property" page
2. Fill out form with valid listing data
3. Click "Create Listing"
4. Verify success message displayed
5. Verify redirected to listings view
6. Verify new listing appears in list

**Expected Result:** Listing created from UI

---

#### TC-WEB-003: Browse Listings (Renter)
**Purpose:** Verify renter can see available listings  
**Prerequisites:** Multiple listings exist, logged in as renter  
**Steps:**
1. Navigate to dashboard/browse listings
2. Verify all listings displayed with title, price, location
3. Click on a listing
4. Verify detail page shows all property information
5. Verify "Request to Rent" button visible

**Expected Result:** Listings browse works

---

#### TC-WEB-004: Navigation - Role-Based Visibility
**Purpose:** Verify menu items appear based on role  
**Prerequisites:** Users of both roles available  
**Steps:**
1. Login as RENTER
2. Verify "List Property" menu item is hidden
3. Verify "View My Requests" menu item visible
4. Logout and login as OWNER
5. Verify "List Property" visible
6. Verify owner-specific options visible

**Expected Result:** Navigation adapts to user role

---

### 3.6 MOBILE TESTS

#### TC-MOBILE-001: Mobile Login Flow
**Purpose:** Verify auth on mobile app  
**Steps:**
1. Launch Android app on emulator
2. Enter valid email and password
3. Select role (RENTER or OWNER)
4. Tap "Login" button
5. Verify successful login message
6. Verify navigated to main activity
7. Verify token stored securely

**Expected Result:** Mobile login works, token persisted

---

#### TC-MOBILE-002: Invalid Credentials
**Purpose:** Verify error handling  
**Steps:**
1. Enter invalid email/password combination
2. Tap login
3. Verify error message displayed
4. Verify not logged in

**Expected Result:** Error displayed, login rejected

---

---

## 4. Test Scripts / Test Steps (Detailed)

### 4.1 Backend API Testing Script

**Tool:** Postman, cURL, or REST client

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@rentease.com",
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "User",
    "role": "RENTER"
  }'

# 2. Login and capture token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@rentease.com",
    "password": "TestPass123!"
  }' | jq '.data.accessToken' > token.txt

# 3. Create a listing (as owner, use different email)
TOKEN=$(cat token.txt)
curl -X POST http://localhost:8080/api/v1/listings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Beautiful 2BR Downtown Apartment",
    "description": "Modern furnished apartment",
    "price": 1500.00,
    "location": "123 Main Street",
    "bedrooms": 2,
    "bathrooms": 1,
    "status": "AVAILABLE"
  }'

# 4. Get all listings
curl -X GET http://localhost:8080/api/v1/listings \
  -H "Authorization: Bearer $TOKEN"

# 5. Create rental request (as renter)
curl -X POST http://localhost:8080/api/v1/requests \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "listingId": 1,
    "rentalStartDate": "2026-05-01",
    "rentalEndDate": "2026-05-31",
    "message": "Very interested!"
  }'
```

### 4.2 Database Validation Script

```sql
-- Verify users table
SELECT COUNT(*) as user_count FROM users;
SELECT * FROM users WHERE email = 'testuser@rentease.com';

-- Verify password is hashed (should not be plaintext)
SELECT email, password FROM users WHERE email = 'testuser@rentease.com';

-- Verify listings
SELECT * FROM listings WHERE owner_id = 1;

-- Verify requests
SELECT * FROM rental_requests WHERE renter_id = 2;

-- Verify ratings
SELECT * FROM ratings WHERE owner_id = 1;

-- Check constraints
SELECT * FROM information_schema.table_constraints 
WHERE table_name = 'rental_requests';
```

---

## 5. Automated Test Cases (JUnit 5 + Spring Boot)

See `AUTOMATED_TESTS.md` for complete test class implementations including:
- AuthControllerTest
- ListingControllerTest
- RentalRequestControllerTest
- RatingControllerTest
- Integration tests

---

## 6. Test Execution Plan

### Phase 1: Unit Tests
- Execute backend service layer tests
- Duration: ~5 minutes
- Command: `mvn test -Dtest=*ServiceTest`

### Phase 2: Integration Tests
- Execute controller + database tests
- Duration: ~10 minutes
- Command: `mvn test`

### Phase 3: API Tests (Postman/Manual)
- Execute backend API endpoints
- Duration: ~30 minutes
- Use provided cURL scripts

### Phase 4: Frontend Tests
- Manual UI testing
- Duration: ~45 minutes
- Check all user workflows

### Phase 5: Mobile Tests
- Manual testing on emulator
- Duration: ~30 minutes
- Auth flows only (Phase 1)

**Total Estimated Time:** ~2 hours per full test cycle

---

## 7. Test Environment Setup

- **Backend:** Running on http://localhost:8080
- **Database:** PostgreSQL on localhost:5432
- **Frontend:** http://localhost:5173
- **Mobile:** Android Emulator (API 34+)
- **Test Data:** Clean database before each cycle

---

## 8. Pass/Fail Criteria

**PASS:**
- All test cases execute without errors
- 100% of functional requirements covered
- No critical bugs identified
- API response codes correct
- Database transactions consistent
- No security issues (JWT, auth)

**FAIL:**
- Any critical bug found
- API endpoints returning unexpected status codes
- Data integrity issues
- Security vulnerabilities
- Regression from previous version

---

## 9. Defect Tracking

All bugs identified during testing will be recorded in `REGRESSION_TEST_RESULTS.md` with:
- Bug ID
- Description
- Severity (Critical/High/Medium/Low)
- Test Case that revealed it
- Status (New/In Progress/Fixed/Closed)

---



---

**Document History:**
- v1.0 - 2026-04-27 - Initial test plan created for Phase 1 completion
