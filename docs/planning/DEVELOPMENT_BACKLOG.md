# Development Jobs / Tasks Backlog

## 🔨 Immediate TODO (Current Sprint)

### High Priority
- [ ] **Implement Edit User API Endpoint** 
  - Backend: Create PUT `/api/v1/admin/users/{userId}` endpoint
  - Allow updating: name, email, mobile, roles
  - Validation and error handling
  
- [ ] **Implement Delete User API Endpoint**
  - Backend: Create DELETE `/api/v1/admin/users/{userId}` endpoint
  - Soft delete vs hard delete decision
  - Check for dependencies before deletion
  
- [ ] **Frontend: Edit User Modal/Form**
  - Create edit user form with validation
  - Connect to Edit User API
  - Update user list after successful edit
  
- [ ] **Frontend: Delete User Confirmation**
  - Enhance delete confirmation dialog
  - Connect to Delete User API
  - Remove user from list after successful deletion

### Medium Priority
- [ ] **User Roles Management**
  - Add/remove roles for users
  - Create role assignment UI
  - Backend API for role management

- [ ] **User Search and Filter**
  - Search users by name, email
  - Filter by roles
  - Pagination for large user lists

- [ ] **User Activity Logs**
  - Track user login/logout
  - Track user actions
  - Display in admin dashboard

## 📋 Backlog Items

### Admin Portal Enhancements
- [ ] **Bulk User Operations**
  - Select multiple users
  - Bulk delete, bulk role assignment
  - Export user list to CSV

- [ ] **User Statistics Dashboard**
  - Total users by role
  - Active users (last 7/30 days)
  - User growth chart

- [ ] **Advanced User Management**
  - Suspend/activate user accounts
  - Reset user passwords (admin action)
  - Account status tracking

### Security & Permissions
- [ ] **Audit Trail**
  - Log all admin actions
  - Track who changed what and when
  - Audit log viewer in admin portal

- [ ] **Permission-based Access Control**
  - Define granular permissions
  - Assign permissions to roles
  - UI based on user permissions

- [ ] **Two-Factor Authentication**
  - Enable 2FA for admin users
  - TOTP/SMS based authentication
  - Recovery codes

### API & Backend Improvements
- [ ] **Rate Limiting**
  - Implement rate limiting on admin endpoints
  - Prevent abuse and DoS attacks

- [ ] **API Documentation**
  - Swagger/OpenAPI documentation
  - Examples and use cases
  - Postman collection

- [ ] **Database Optimization**
  - Add indexes for frequently queried fields
  - Query optimization
  - Connection pooling tuning

### User Experience
- [ ] **Toast Notifications**
  - Success messages for operations
  - Error notifications with details
  - Loading states

- [ ] **Keyboard Shortcuts**
  - Quick navigation in admin portal
  - Escape to close modals
  - Ctrl+K for search

- [ ] **Mobile Responsive Design**
  - Optimize admin UI for tablets
  - Mobile-friendly tables
  - Touch-friendly controls

### Testing
- [ ] **Unit Tests**
  - Test admin controllers
  - Test user service methods
  - Test authentication helper

- [ ] **Integration Tests**
  - Test admin API endpoints
  - Test role-based access
  - Test error scenarios

- [ ] **E2E Tests**
  - Test admin user flows
  - Test edit/delete operations
  - Test modal interactions

### DevOps & Monitoring
- [ ] **Logging & Monitoring**
  - Centralized logging
  - Error tracking (Sentry/Rollbar)
  - Performance monitoring

- [ ] **Backup & Recovery**
  - Automated database backups
  - User data export/import
  - Disaster recovery plan

- [ ] **CI/CD Pipeline**
  - Automated testing on PR
  - Automated deployment
  - Rolling updates

## 📅 Future Enhancements

### Advanced Features
- [ ] User profile management
- [ ] Email notifications to users
- [ ] User onboarding workflow
- [ ] Custom user fields/metadata
- [ ] User groups/teams
- [ ] Scheduled jobs management
- [ ] System health monitoring
- [ ] Database query builder for admins
- [ ] Custom reports generation

### Integrations
- [ ] SSO/SAML integration
- [ ] LDAP/Active Directory integration
- [ ] Slack notifications for admin actions
- [ ] Webhook support for external systems

---

## ✅ Completed
- [x] Create admin portal UI structure
- [x] Add admin navigation in sidebar
- [x] Create admin users list page
- [x] Add 3-dots action menu in users table
- [x] Make user names clickable to view details
- [x] Create user details modal with edit/delete buttons
- [x] Remove net worth option from admin UI
- [x] Add role-based sidebar visibility
- [x] Fix full-screen layout

---

**Last Updated:** 31 January 2026
