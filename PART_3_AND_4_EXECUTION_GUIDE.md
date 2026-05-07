# Part 3 & Part 4: Test Plan & Regression Testing Execution Guide

**Last Updated:** April 27, 2026

## Quick Navigation
- [Part 3: Test Plan Creation](#part-3-test-plan-creation)
- [Part 4: Full Regression Testing](#part-4-full-regression-testing)
- [Execution Workflow](#execution-workflow)

---

## Part 3: Test Plan Creation ✅ COMPLETE

### What Was Created

The comprehensive test plan has been created and includes:

**📄 Main Document:** [`TEST_PLAN.md`](TEST_PLAN.md)
- Complete testing strategy
- Functional requirements coverage matrix
- 20+ detailed test cases organized by feature
- Test scripts and procedures
- Pass/fail criteria

**🧪 Automated Test Files:**
1. [`backend/src/test/java/edu/cit/sabornido/rentease/features/auth/AuthControllerTest.java`](backend/src/test/java/edu/cit/sabornido/rentease/features/auth/AuthControllerTest.java)
   - 7 test cases (TC-AUTH-001 through TC-AUTH-007)
   - Tests: Registration, login, JWT validation, role-based access

2. [`backend/src/test/java/edu/cit/sabornido/rentease/features/listings/ListingControllerTest.java`](backend/src/test/java/edu/cit/sabornido/rentease/features/listings/ListingControllerTest.java)
   - 8 test cases (TC-LISTING-001 through TC-LISTING-008)
   - Tests: Create, read, update, delete listings with permission checks

3. [`backend/src/test/java/edu/cit/sabornido/rentease/features/requests/RentalRequestControllerTest.java`](backend/src/test/java/edu/cit/sabornido/rentease/features/requests/RentalRequestControllerTest.java)
   - 5 test cases (TC-REQUEST-001 through TC-REQUEST-005)
   - Tests: Create requests, approve, decline with role validation

4. [`backend/src/test/java/edu/cit/sabornido/rentease/features/ratings/RatingControllerTest.java`](backend/src/test/java/edu/cit/sabornido/rentease/features/ratings/RatingControllerTest.java)
   - 5 test cases (TC-RATING-001 through TC-RATING-005)
   - Tests: Create ratings, validation, score boundaries

**📊 Test Results Template:** [`REGRESSION_TEST_RESULTS.md`](REGRESSION_TEST_RESULTS.md)
- Template for recording test execution results
- Space for bug reporting
- Regression analysis sections
- Sign-off documentation

### Test Coverage Summary

| Module | Test Cases | Backend | Web | Mobile |
|--------|-----------|---------|-----|--------|
| **Authentication** | 7 | ✅ Auto | ✅ Manual | ✅ Manual |
| **Listings** | 8 | ✅ Auto | ✅ Manual | ❌ N/A |
| **Requests** | 5 | ✅ Auto | ✅ Manual | ❌ N/A |
| **Ratings** | 5 | ✅ Auto | ✅ Manual | ❌ N/A |
| **Frontend UI** | 4 | N/A | ✅ Manual | N/A |
| **Mobile Auth** | 2 | N/A | N/A | ✅ Manual |
| **TOTAL** | **31** | **25 Automated** | **14 Manual** | **2 Manual** |

---

## Part 4: Full Regression Testing

### Prerequisites

Before starting regression testing, ensure:

```
✅ Backend is running:
   - Start: cd backend && mvn spring-boot:run
   - Verify: http://localhost:8080/api/v1/health (if available)
   
✅ Database is accessible:
   - PostgreSQL running on localhost:5432
   - RentEase database created
   - Schema initialized

✅ Web frontend is available:
   - Start: cd web && npm install && npm run dev
   - Verify: http://localhost:5173

✅ Mobile emulator (optional for Phase 1):
   - Android Emulator running (API 34+)
   - App built and installed
```

### Step-by-Step Regression Testing Workflow

---

## Execution Workflow

### Phase 1: Setup & Preparation

**Duration:** 5 minutes

```bash
# 1. Clone/update repository to latest main branch
git checkout main
git pull origin main

# 2. Start backend
cd backend
mvn clean install
mvn spring-boot:run
# Wait for: "Started RenteaseApplication in X seconds"

# 3. In new terminal, start web frontend
cd web
npm install
npm run dev
# Look for: "Local: http://localhost:5173"

# 4. Verify backend health
curl http://localhost:8080/api/v1/health

# 5. Open browser to http://localhost:5173 and verify no console errors
```

### Phase 2: Automated Backend Tests

**Duration:** 10-15 minutes

```bash
# Run all tests (recommended)
cd backend
mvn clean test

# OR run specific test classes:
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=ListingControllerTest
mvn test -Dtest=RentalRequestControllerTest
mvn test -Dtest=RatingControllerTest

# OR run all but skip slow tests:
mvn test -DskipIntegrationTests=false
```

**Expected Output:**
```
[INFO] Running edu.cit.sabornido.rentease.features.auth.AuthControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running edu.cit.sabornido.rentease.features.listings.ListingControllerTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running edu.cit.sabornido.rentease.features.requests.RentalRequestControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running edu.cit.sabornido.rentease.features.ratings.RatingControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: XX.XXX s
```

**Recording Results:**
1. Copy test output
2. Open `REGRESSION_TEST_RESULTS.md`
3. Paste output in "Appendix A: Detailed Test Logs"
4. Mark all automated tests as ✅ PASS (or ❌ FAIL if there are failures)
5. Update Executive Summary metrics

---

### Phase 3: Manual API Testing

**Duration:** 30 minutes

Using Postman, REST Client extension, or cURL commands from TEST_PLAN.md:

#### Test Set 1: Authentication (TC-AUTH-001 through TC-AUTH-007)

```bash
# 1. Register a new renter
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "regressiontest.renter@rentease.com",
    "password": "RegTest123!",
    "firstName": "Regression",
    "lastName": "Renter",
    "role": "RENTER"
  }'
# Expected: 201 Created, user object returned
# ☐ TC-AUTH-001 PASS

# 2. Attempt duplicate registration (same email)
# Expected: 400 Bad Request, error about duplicate email
# ☐ TC-AUTH-002 PASS

# 3. Test invalid email format
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "notanemail",
    "password": "RegTest123!",
    "firstName": "Bad",
    "lastName": "Email",
    "role": "RENTER"
  }'
# Expected: 400 Bad Request
# ☐ TC-AUTH-003 PASS

# 4. Login successfully
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "regressiontest.renter@rentease.com",
    "password": "RegTest123!"
  }' | jq '.'
# Expected: 200 OK with accessToken and refreshToken
# ☐ TC-AUTH-004 PASS

# 5. Login with wrong password
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "regressiontest.renter@rentease.com",
    "password": "WrongPassword123!"
  }'
# Expected: 401 Unauthorized
# ☐ TC-AUTH-005 PASS

# 6. Test token validation - access without token
curl -X GET http://localhost:8080/api/v1/listings
# Expected: 401 Unauthorized
# ☐ TC-AUTH-006 PASS

# 7. Role-based access (covered in Integration tests)
# ☐ TC-AUTH-007 PASS
```

**Record in REGRESSION_TEST_RESULTS.md:**
- Mark TC-AUTH-001 through TC-AUTH-007 as PASS

#### Test Set 2: Listings (TC-LISTING-001 through TC-LISTING-008)

```bash
# First, register owner and get token
OWNER_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "regressiontest.owner@rentease.com",
    "password": "RegTest123!"
  }' | jq -r '.data.accessToken')

# 1. Create listing
curl -X POST http://localhost:8080/api/v1/listings \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Apartment",
    "description": "Regression test property",
    "price": 1500.00,
    "location": "123 Test St",
    "bedrooms": 2,
    "bathrooms": 1,
    "status": "AVAILABLE"
  }' | jq '.data.id'
# Expected: 201 Created
# Save the ID as LISTING_ID
# ☐ TC-LISTING-001 PASS

# 2. Missing required field
curl -X POST http://localhost:8080/api/v1/listings \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "No title",
    "price": 1500.00
  }'
# Expected: 400 Bad Request
# ☐ TC-LISTING-002 PASS

# 3. Get all listings
curl -X GET http://localhost:8080/api/v1/listings \
  -H "Authorization: Bearer $OWNER_TOKEN" | jq '.data | length'
# Expected: 200 OK, array with listings
# ☐ TC-LISTING-003 PASS

# 4. Get specific listing
curl -X GET http://localhost:8080/api/v1/listings/$LISTING_ID \
  -H "Authorization: Bearer $OWNER_TOKEN"
# Expected: 200 OK with listing details
# ☐ TC-LISTING-004 PASS

# 5. Get non-existent listing
curl -X GET http://localhost:8080/api/v1/listings/99999 \
  -H "Authorization: Bearer $OWNER_TOKEN"
# Expected: 404 Not Found
# ☐ TC-LISTING-005 PASS

# 6. Update listing
curl -X PUT http://localhost:8080/api/v1/listings/$LISTING_ID \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "price": 1600.00
  }'
# Expected: 200 OK
# ☐ TC-LISTING-006 PASS

# 7. Update as different user (should fail)
# Use RENTER_TOKEN instead of OWNER_TOKEN
# Expected: 403 Forbidden
# ☐ TC-LISTING-007 PASS

# 8. Delete listing
curl -X DELETE http://localhost:8080/api/v1/listings/$LISTING_ID \
  -H "Authorization: Bearer $OWNER_TOKEN"
# Expected: 200/204 OK
# Verify with GET: should return 404
# ☐ TC-LISTING-008 PASS
```

**Record in REGRESSION_TEST_RESULTS.md:**
- Mark TC-LISTING-001 through TC-LISTING-008 as PASS

#### Test Set 3: Rental Requests (TC-REQUEST-001 through TC-REQUEST-005)

```bash
# 1. Create rental request (as renter)
curl -X POST http://localhost:8080/api/v1/requests \
  -H "Authorization: Bearer $RENTER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "listingId": '$LISTING_ID',
    "rentalStartDate": "2026-05-01",
    "rentalEndDate": "2026-05-31",
    "message": "Interested in renting"
  }' | jq '.data.id'
# Expected: 201 Created
# Save as REQUEST_ID
# ☐ TC-REQUEST-001 PASS

# 2. Get owner's requests
curl -X GET http://localhost:8080/api/v1/requests \
  -H "Authorization: Bearer $OWNER_TOKEN" | jq '.data | length'
# Expected: 200 OK, should include the request
# ☐ TC-REQUEST-002 PASS

# 3. Get renter's requests
curl -X GET http://localhost:8080/api/v1/requests \
  -H "Authorization: Bearer $RENTER_TOKEN" | jq '.data | length'
# Expected: 200 OK, shows renter's requests
# ☐ TC-REQUEST-003 PASS

# 4. Approve request (as owner)
curl -X PUT http://localhost:8080/api/v1/requests/$REQUEST_ID \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED"}'
# Expected: 200 OK with status APPROVED
# ☐ TC-REQUEST-004 PASS

# 5. Decline request (create new one first)
# Create another request with different dates
# Then decline it
curl -X PUT http://localhost:8080/api/v1/requests/$REQUEST_ID_2 \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "DECLINED"}'
# Expected: 200 OK with status DECLINED
# ☐ TC-REQUEST-005 PASS
```

**Record in REGRESSION_TEST_RESULTS.md:**
- Mark TC-REQUEST-001 through TC-REQUEST-005 as PASS

#### Test Set 4: Ratings (TC-RATING-001 through TC-RATING-003)

```bash
# 1. Create rating
curl -X POST http://localhost:8080/api/v1/ratings \
  -H "Authorization: Bearer $RENTER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "ownerId": '$OWNER_ID',
    "rating": 5,
    "comment": "Excellent service!"
  }'
# Expected: 201 Created
# ☐ TC-RATING-001 PASS

# 2. Invalid rating (score 10)
curl -X POST http://localhost:8080/api/v1/ratings \
  -H "Authorization: Bearer $RENTER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "ownerId": '$OWNER_ID',
    "rating": 10,
    "comment": "Invalid"
  }'
# Expected: 400 Bad Request
# ☐ TC-RATING-002 PASS

# 3. Get owner's ratings
curl -X GET "http://localhost:8080/api/v1/ratings?ownerId=$OWNER_ID" \
  -H "Authorization: Bearer $RENTER_TOKEN"
# Expected: 200 OK, array of ratings
# ☐ TC-RATING-003 PASS
```

**Record in REGRESSION_TEST_RESULTS.md:**
- Mark TC-RATING-001 through TC-RATING-003 as PASS

---

### Phase 4: Web Frontend Manual Testing

**Duration:** 30-45 minutes

Open browser to http://localhost:5173

#### TC-WEB-001: User Login Flow
```
1. Open http://localhost:5173
2. Verify Landing/Login page displays
3. Enter valid credentials:
   - Email: regressiontest.renter@rentease.com
   - Password: RegTest123!
4. Click Login
5. Verify redirected to Dashboard
6. Open DevTools (F12) → Storage → Cookies/LocalStorage
7. Verify JWT token is stored
8. Check Network tab: verify token used in subsequent requests
9. Record: ☐ TC-WEB-001 PASS
```

#### TC-WEB-002: Create Listing Flow (Owner)
```
1. Login as owner (regressiontest.owner@rentease.com)
2. Navigate to "List Property" or create listing page
3. Fill in listing form:
   - Title: "Regression Test Property"
   - Price: 2000
   - Location: Test Location
   - Bedrooms: 2
   - Bathrooms: 1
4. Click Create/Submit
5. Verify success message or redirect to listings
6. Verify listing appears in "My Listings"
7. Record: ☐ TC-WEB-002 PASS
```

#### TC-WEB-003: Browse Listings (Renter)
```
1. Login as renter
2. View dashboard/listings page
3. Verify multiple listings display
4. Verify each shows: title, price, location
5. Click on a listing
6. Verify detail page shows full information
7. Verify "Request to Rent" button is visible
8. Record: ☐ TC-WEB-003 PASS
```

#### TC-WEB-004: Role-Based Navigation
```
1. Login as RENTER
2. Check navigation/menu
3. Verify "List Property" is HIDDEN
4. Verify "My Requests" is VISIBLE
5. Logout
6. Login as OWNER
7. Verify "List Property" is VISIBLE
8. Verify owner-specific menu items visible
9. Record: ☐ TC-WEB-004 PASS
```

**Capture Screenshots:**
- Save screenshots of each page for documentation
- Note any UI issues or inconsistencies

**Record in REGRESSION_TEST_RESULTS.md:**
- Mark TC-WEB-001 through TC-WEB-004 as PASS (or FAIL with notes)

---

### Phase 5: Mobile Testing (Optional - Phase 1 Only)

**Duration:** 30 minutes (if performing)

```bash
# 1. Start Android Emulator
emulator -avd YourEmulatorName

# 2. Install app
cd mobile
./gradlew installDebug

# 3. Launch app in emulator

# TC-MOBILE-001: Login Flow
- Open app
- Enter: regressiontest.renter@rentease.com
- Password: RegTest123!
- Select role: RENTER
- Tap Login
- Verify: Login successful
- Verify: Token stored in SharedPreferences
- Record: ☐ TC-MOBILE-001 PASS

# TC-MOBILE-002: Invalid Credentials
- Try login with wrong password
- Verify: Error message displayed
- Verify: Not logged in
- Record: ☐ TC-MOBILE-002 PASS
```

---

### Phase 6: Database Validation

**Duration:** 10 minutes

```bash
# Connect to PostgreSQL
psql -U postgres -d rentease

# Verify user data
SELECT COUNT(*) FROM users;
SELECT email, role FROM users LIMIT 5;

# Verify password is hashed (not plaintext)
SELECT email, password FROM users WHERE email LIKE '%regressiontest%';

# Verify listings
SELECT * FROM listings LIMIT 5;

# Verify requests
SELECT * FROM rental_requests LIMIT 5;

# Verify ratings
SELECT * FROM ratings LIMIT 5;

# Check constraints
\d listings
\d rental_requests

# Record observations in REGRESSION_TEST_RESULTS.md
```

---

### Phase 7: Results Summary & Analysis

**Duration:** 15 minutes

In `REGRESSION_TEST_RESULTS.md`:

```markdown
## Executive Summary (Update)

| Metric | Value |
|--------|-------|
| **Total Test Cases Executed** | 31 |
| **Passed** | [Your number] |
| **Failed** | [Your number] |
| **Pass Rate** | [Percentage] |
| **Critical Bugs Found** | [Count] |
| **Test Status** | ✅ COMPLETE or ❌ WITH ISSUES |
```

**Regression Analysis:**
- Compare results with previous test run
- Document any new failures or improved pass rates
- Identify trends

**Bug Documentation:**
- For each failed test, create a BUG entry
- Include severity, reproduction steps, expected vs actual

**Sign-Off:**
- QA Lead approves/rejects
- Development Lead reviews bugs
- Project Manager authorizes release or requests fixes

---

## Full Test Execution Command (All-in-One)

**Quick 1-hour full regression test:**

```bash
# Terminal 1: Start backend
cd backend
mvn clean test  # Automated tests
mvn spring-boot:run  # For manual tests

# Terminal 2: Start frontend
cd web
npm install
npm run dev

# Terminal 3: Execute test scripts
# Use cURL commands from TEST_PLAN.md sections above

# Then:
# 1. Open browser to http://localhost:5173
# 2. Test web UI manually
# 3. Test mobile if applicable
# 4. Run database validation queries
# 5. Record all results in REGRESSION_TEST_RESULTS.md
```

---

## Troubleshooting

### Backend Tests Fail
```
Problem: mvn test returns failures
Solution:
1. Check if backend is already running on port 8080
2. Kill process: lsof -ti:8080 | xargs kill -9
3. Verify database connection: check application.yaml
4. Check test database is clean
5. Run: mvn clean test (not just mvn test)
```

### API Calls Return 401
```
Problem: JWT token not working
Solution:
1. Verify token hasn't expired (check exp claim)
2. Verify Authorization header format: "Bearer {token}"
3. Verify token from login response was copied correctly
4. Check no spaces in token
```

### Web Frontend Won't Load
```
Problem: http://localhost:5173 shows error
Solution:
1. Verify npm dev server is running
2. Check for build errors in terminal
3. Clear browser cache (Ctrl+Shift+Delete)
4. Check browser console for errors (F12)
5. Run: npm install && npm run dev
```

### Database Connection Failed
```
Problem: Backend won't connect to PostgreSQL
Solution:
1. Verify PostgreSQL is running
2. Check connection string in application.yaml
3. Verify database exists: psql -l
4. Check credentials in application.yaml
```

---

## Success Criteria for Release

✅ **Regression Testing PASS if:**
- All 25 automated backend tests pass
- All 14 API tests pass (manual or automated)
- All 4 web frontend tests pass
- All 2 mobile tests pass (if tested)
- Zero critical bugs
- All data integrity checks pass
- No new regressions from previous build

❌ **Regression Testing FAIL if:**
- Any automated test fails
- Critical bugs discovered
- Data integrity issues found
- Security vulnerabilities detected

---

## Next Steps After Regression Testing

1. **If PASS:**
   - Sign-off document
   - Merge feature branch to main
   - Tag release version
   - Deploy to staging

2. **If FAIL:**
   - Document all bugs in issue tracker
   - Assign to developers
   - Fix bugs in feature branch
   - Re-run regression tests
   - Continue until all tests pass

---

## Reference Documents

- [`TEST_PLAN.md`](TEST_PLAN.md) - Complete test plan with all test cases
- [`REGRESSION_TEST_RESULTS.md`](REGRESSION_TEST_RESULTS.md) - Template to fill during testing
- `backend/pom.xml` - Maven configuration for tests
- `backend/src/test/` - All automated test files

---

**Document Version:** 1.0  
**Last Updated:** April 27, 2026  
**Next Review:** After first regression test cycle
