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
    // flags: { FEATURE_NAME: true }  — only truly-enabled flags appear here
    const [features, setFeatures] = useState({});
    // modules: [{ module, displayName, enabled, category, subFeatures: [...] }]
    const [modules, setModules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadFeatures = async () => {
        try {
            const user = JSON.parse(localStorage.getItem('user'));
            if (!user || !user.token) {
                setLoading(false);
                return;
            }

            // Load flat enabled list + module hierarchy in parallel
            const [enabledFeatures, moduleHierarchy] = await Promise.all([
                featureApi.getEnabledFeatures(user.token),
                featureApi.getModuleHierarchy(user.token).catch(() => []),
            ]);

            const featureMap = {};
            if (Array.isArray(enabledFeatures)) {
                enabledFeatures.forEach(feature => {
                    const featureName = typeof feature === 'string' ? feature : feature.name;
                    if (featureName) featureMap[featureName] = true;
                });
            }

            setFeatures(featureMap);
            setModules(Array.isArray(moduleHierarchy) ? moduleHierarchy : []);
            setError(null);
        } catch (err) {
            console.error('Failed to load features:', err);
            setError(err.message);
            setFeatures({});
            setModules([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadFeatures();
        const interval = setInterval(loadFeatures, 5 * 60 * 1000);
        return () => clearInterval(interval);
    }, []);

    /** True only if the named flag is in the enabled set (backend already resolves parent). */
    const isFeatureEnabled = (featureName) => features[featureName] === true;

    /**
     * Returns the sub-features array for a given module name.
     * Each item: { name, displayName, description, enabled }
     */
    const getSubFeatures = (moduleName) => {
        const mod = modules.find(m => m.module === moduleName);
        return mod ? mod.subFeatures : [];
    };

    /**
     * True if the module is enabled AND the specific sub-feature is enabled.
     * Equivalent to isFeatureEnabled(subFeatureName) since the backend already
     * accounts for the parent, but provided as a documented helper.
     */
    const isSubFeatureEnabled = (moduleName, subFeatureName) =>
        isFeatureEnabled(moduleName) && isFeatureEnabled(subFeatureName);

    const refreshFeatures = async () => {
        setLoading(true);
        await loadFeatures();
    };

    return (
        <FeatureContext.Provider value={{
            features,
            modules,
            loading,
            error,
            isFeatureEnabled,
            isSubFeatureEnabled,
            getSubFeatures,
            refreshFeatures,
        }}>
            {children}
        </FeatureContext.Provider>
    );
};
