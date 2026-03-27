# Admin Portal Documentation

## Overview
The admin portal provides privileged access to system administrators to view and manage user data across the platform.

## Admin Roles
- `ROLE_ADMIN` - Standard administrator with full access to admin endpoints
- `ROLE_SUPER_ADMIN` - Super administrator (if needed for additional privileges)

## Admin Endpoints

### 1. Admin User Management (`/api/v1/admin/users`)

#### Get All Users
```bash
GET /api/v1/admin/users
Authorization: Bearer <admin-jwt-token>
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "mobileNumber": "1234567890",
    "roles": ["ROLE_USER", "ROLE_ADMIN"]
  }
]
```

#### Get User by ID
```bash
GET /api/v1/admin/users/{userId}
Authorization: Bearer <admin-jwt-token>
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "mobileNumber": "1234567890",
  "roles": ["ROLE_USER"]
}
```

## Security Features

### 1. Authorization
- All admin endpoints require `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`
- Uses Spring Security's `@PreAuthorize` annotation
- Validates admin access using `AuthenticationHelper.validateAdminAccess()`

### 2. User Endpoints Enhancement
- Regular user endpoints (`/api/v1/net-worth/{userId}`) now support admin access
- Admins can view any user's data through regular endpoints
- Non-admin users can only view their own data

### 3. Data Security
- User passwords are excluded from admin responses
- Uses dedicated DTOs (`UserAdminDTO`) to control data exposure
- All requests require valid JWT authentication

## Usage Examples

### Admin Viewing Another User's Net Worth
```bash
# As admin user
curl -X 'GET' \
  'http://localhost:8082/api/v1/net-worth/1' \
  -H 'Authorization: Bearer <admin-jwt-token>'
# ✅ Success - Admin can view any user's data

# As regular user trying to view another user
curl -X 'GET' \
  'http://localhost:8082/api/v1/net-worth/2' \
  -H 'Authorization: Bearer <regular-user-jwt-token>'
# ❌ 403 Forbidden - Can only view own data
```

### Admin-Specific Endpoints
```bash
# Get all users in system
curl -X 'GET' \
  'http://localhost:8082/api/v1/admin/users' \
  -H 'Authorization: Bearer <admin-jwt-token>'

# Get specific user details
curl -X 'GET' \
  'http://localhost:8082/api/v1/admin/users/1' \
  -H 'Authorization: Bearer <admin-jwt-token>'

# Get any user's net worth via admin endpoint
curl -X 'GET' \
  'http://localhost:8082/api/v1/admin/net-worth/1' \
  -H 'Authorization: Bearer <admin-jwt-token>'
```

## Implementation Details

### Files Created/Modified

1. **AuthenticationHelper** - Updated with admin validation
   - `isAdmin()` - Checks if current user has admin role
   - `validateAdminAccess()` - Throws exception if not admin
   - `validateUserAccess()` - Now allows admin bypass

2. **AdminNetWorthController** - Admin net worth endpoint
   - `/api/v1/admin/net-worth/{userId}` - View any user's net worth

3. **AdminUsersController** - Admin user management
   - `/api/v1/admin/users` - List all users
   - `/api/v1/admin/users/{userId}` - Get specific user

4. **UserAdminDTO** - Safe data transfer object
   - Excludes sensitive information like passwords

5. **UserReadService** - Enhanced user queries
   - Added `getAllUsers()` method for admin use

## Testing

To test admin functionality:

1. **Create admin user** (via database or registration):
```sql
-- Assign admin role to user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.email = 'admin@example.com' 
AND r.name = 'ROLE_ADMIN';
```

2. **Login as admin** and get JWT token

3. **Test admin endpoints** with the admin token

4. **Verify access control** by testing with non-admin token
