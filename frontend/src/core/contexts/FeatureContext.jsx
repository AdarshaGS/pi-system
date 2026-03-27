import { createContext, useContext, useState, useEffect } from 'react';
import { featureApi } from '@/core/api';

const FeatureContext = createContext();

export const useFeatures = () => {
    const context = useContext(FeatureContext);
    if (!context) {
        throw new Error('useFeatures must be used within FeatureProvider');
    }
    return context;
};

export const FeatureProvider = ({ children }) => {
    const [features, setFeatures] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadFeatures = async () => {
        try {
            const user = JSON.parse(localStorage.getItem('user'));
            if (!user || !user.token) {
                setLoading(false);
                return;
            }

            const enabledFeatures = await featureApi.getEnabledFeatures(user.token);
            
            // Convert array of feature names/objects to object for quick lookup
            const featureMap = {};
            if (Array.isArray(enabledFeatures)) {
                enabledFeatures.forEach(feature => {
                    const featureName = typeof feature === 'string' ? feature : feature.name;
                    if (featureName) {
                        featureMap[featureName] = true;
                    }
                });
            }
            
            setFeatures(featureMap);
            setError(null);
        } catch (err) {
            console.error('Failed to load features:', err);
            setError(err.message);
            // Set empty features on error - all features disabled by default
            setFeatures({});
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadFeatures();
        
        // Reload features every 5 minutes
        const interval = setInterval(loadFeatures, 5 * 60 * 1000);
        return () => clearInterval(interval);
    }, []);

    const isFeatureEnabled = (featureName) => {
        return features[featureName] === true;
    };

    const refreshFeatures = async () => {
        setLoading(true);
        await loadFeatures();
    };

    return (
        <FeatureContext.Provider value={{ 
            features, 
            loading, 
            error, 
            isFeatureEnabled,
            refreshFeatures 
        }}>
            {children}
        </FeatureContext.Provider>
    );
};
