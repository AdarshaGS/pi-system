import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../api';

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [submitted, setSubmitted] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setLoading(true);
        setError('');

        try {
            await authApi.forgotPassword({ email, password, confirmPassword });
            setSubmitted(true);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1>Reset Password</h1>
                    <p>Enter your email and new password</p>
                </div>

                {error && <div className="form-error" style={{ marginBottom: '16px', textAlign: 'center' }}>{error}</div>}

                {submitted ? (
                    <div style={{ textAlign: 'center' }}>
                        <p style={{ color: 'var(--success-color)', marginBottom: '20px' }}>
                            Password updated successfully!
                        </p>
                        <Link to="/login" className="auth-button" style={{ display: 'block', textDecoration: 'none' }}>
                            Back to Login
                        </Link>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <input
                                type="email"
                                id="email"
                                placeholder="Enter your email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                disabled={loading}
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="password">New Password</label>
                            <input
                                type="password"
                                id="password"
                                placeholder="Enter new password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                disabled={loading}
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="confirmPassword">Confirm New Password</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                placeholder="Confirm new password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                                disabled={loading}
                            />
                        </div>
                        <button type="submit" className="auth-button" disabled={loading}>
                            {loading ? 'Updating...' : 'Update Password'}
                        </button>
                    </form>
                )}
                {!submitted && (
                    <div className="auth-footer">
                        <p>
                            <Link to="/login">Back to Login</Link>
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ForgotPassword;
