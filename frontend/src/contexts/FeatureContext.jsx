import { createContext, useContext, useState, useEffect } from 'react';
import { featureApi } from '../api';

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
            
            // Convert array of feature names to object for quick lookup
            const featureMap = {};
            enabledFeatures.forEach(featureName => {
                featureMap[featureName] = true;
            });
            
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
