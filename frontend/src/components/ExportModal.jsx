import React, { useState } from 'react';
import './ExportModal.css';

const ExportModal = ({ show, onClose, onExport, exportType = 'expenses', categories = [] }) => {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [selectedCategories, setSelectedCategories] = useState([]);
    const [format, setFormat] = useState('csv');
    const [loading, setLoading] = useState(false);

    if (!show) return null;

    const handleCategoryToggle = (category) => {
        setSelectedCategories(prev => 
            prev.includes(category) 
                ? prev.filter(c => c !== category)
                : [...prev, category]
        );
    };

    const handleSelectAllCategories = () => {
        if (selectedCategories.length === categories.length) {
            setSelectedCategories([]);
        } else {
            setSelectedCategories(categories.map(c => c.value || c));
        }
    };

    const handleExport = async () => {
        setLoading(true);
        try {
            const params = {
                startDate: startDate || undefined,
                endDate: endDate || undefined,
                category: selectedCategories.length > 0 ? selectedCategories.join(',') : undefined,
                format
            };
            await onExport(params);
            onClose();
            // Reset form
            setStartDate('');
            setEndDate('');
            setSelectedCategories([]);
            setFormat('csv');
        } catch (error) {
            console.error('Export failed:', error);
            alert('Export failed: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Export {exportType === 'expenses' ? 'Expenses' : 'Income'}</h2>
                    <button className="close-button" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    {/* Date Range */}
                    <div className="form-group">
                        <label>Date Range (Optional)</label>
                        <div className="date-range-inputs">
                            <input
                                type="date"
                                value={startDate}
                                onChange={(e) => setStartDate(e.target.value)}
                                placeholder="Start Date"
                            />
                            <span className="date-separator">to</span>
                            <input
                                type="date"
                                value={endDate}
                                onChange={(e) => setEndDate(e.target.value)}
                                placeholder="End Date"
                            />
                        </div>
                    </div>

                    {/* Category Filter (only for expenses) */}
                    {exportType === 'expenses' && categories.length > 0 && (
                        <div className="form-group">
                            <label>Categories (Optional)</label>
                            <div className="category-select-header">
                                <button 
                                    type="button"
                                    className="select-all-btn"
                                    onClick={handleSelectAllCategories}
                                >
                                    {selectedCategories.length === categories.length ? 'Deselect All' : 'Select All'}
                                </button>
                                <span className="selected-count">
                                    {selectedCategories.length} selected
                                </span>
                            </div>
                            <div className="category-checkboxes">
                                {categories.map((category) => {
                                    const value = category.value || category;
                                    const label = category.label || category;
                                    return (
                                        <label key={value} className="checkbox-label">
                                            <input
                                                type="checkbox"
                                                checked={selectedCategories.includes(value)}
                                                onChange={() => handleCategoryToggle(value)}
                                            />
                                            <span>{label}</span>
                                        </label>
                                    );
                                })}
                            </div>
                        </div>
                    )}

                    {/* Format Selection */}
                    <div className="form-group">
                        <label>Export Format</label>
                        <div className="format-options">
                            <label className="radio-label">
                                <input
                                    type="radio"
                                    name="format"
                                    value="csv"
                                    checked={format === 'csv'}
                                    onChange={(e) => setFormat(e.target.value)}
                                />
                                <span>CSV (Comma-separated values)</span>
                            </label>
                            <label className="radio-label">
                                <input
                                    type="radio"
                                    name="format"
                                    value="excel"
                                    checked={format === 'excel'}
                                    onChange={(e) => setFormat(e.target.value)}
                                />
                                <span>Excel (.xlsx)</span>
                            </label>
                            {exportType === 'expenses' && (
                                <label className="radio-label">
                                    <input
                                        type="radio"
                                        name="format"
                                        value="pdf"
                                        checked={format === 'pdf'}
                                        onChange={(e) => setFormat(e.target.value)}
                                    />
                                    <span>PDF Report</span>
                                </label>
                            )}
                        </div>
                    </div>

                    {/* Export Info */}
                    <div className="export-info">
                        <p>
                            {selectedCategories.length > 0 
                                ? `Exporting ${selectedCategories.length} ${selectedCategories.length === 1 ? 'category' : 'categories'}`
                                : 'Exporting all categories'}
                            {startDate && endDate 
                                ? ` from ${startDate} to ${endDate}`
                                : startDate 
                                    ? ` from ${startDate}`
                                    : endDate 
                                        ? ` until ${endDate}`
                                        : ' (all dates)'}
                        </p>
                    </div>
                </div>

                <div className="modal-footer">
                    <button 
                        className="btn-secondary" 
                        onClick={onClose}
                        disabled={loading}
                    >
                        Cancel
                    </button>
                    <button 
                        className="btn-primary" 
                        onClick={handleExport}
                        disabled={loading}
                    >
                        {loading ? 'Exporting...' : 'Export'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ExportModal;
