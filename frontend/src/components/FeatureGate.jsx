import { useFeatures } from '../contexts/FeatureContext';
import { AlertCircle } from 'lucide-react';

const FeatureGate = ({ feature, children, fallback = null, showDisabledMessage = false }) => {
    const { isFeatureEnabled, loading } = useFeatures();

    if (loading) {
        return fallback || null;
    }

    if (!isFeatureEnabled(feature)) {
        if (showDisabledMessage) {
            return (
                <div style={{
                    padding: '40px 20px',
                    textAlign: 'center',
                    color: '#666'
                }}>
                    <AlertCircle size={48} style={{ color: '#ffc107', marginBottom: '20px' }} />
                    <h2>Feature Not Available</h2>
                    <p>The {feature} feature is currently disabled.</p>
                    <p style={{ marginTop: '10px', fontSize: '14px' }}>
                        Please contact your administrator to enable this feature.
                    </p>
                </div>
            );
        }
        return fallback;
    }

    return children;
};

export default FeatureGate;
