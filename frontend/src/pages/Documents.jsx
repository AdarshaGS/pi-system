/**
 * Documents Management Dashboard
 * 
 * Features:
 * - Upload documents with drag-and-drop
 * - Category-based organization
 * - Document preview
 * - Search and filter
 * - Download and delete
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import React, { useState, useEffect, useRef } from 'react';
import {
    getUserDocuments,
    getDocumentsByCategory,
    uploadDocument,
    downloadDocument,
    deleteDocument,
    searchDocuments,
    getDocumentStats
} from '../api/documentsApi';
import DocumentCard from '../components/DocumentCard';
import './Documents.css';
import {
    FaFolder,
    FaPlus,
    FaSearch,
    FaFilter,
    FaFileAlt,
    FaCloudUploadAlt
} from 'react-icons/fa';

const Documents = () => {
    const [documents, setDocuments] = useState([]);
    const [filteredDocuments, setFilteredDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [uploading, setUploading] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState('ALL');
    const [searchQuery, setSearchQuery] = useState('');
    const [stats, setStats] = useState(null);
    const [dragActive, setDragActive] = useState(false);
    
    const fileInputRef = useRef(null);
    const userId = localStorage.getItem('userId') || '1';

    const categories = [
        { value: 'ALL', label: 'All Documents', icon: '📁' },
        { value: 'AGREEMENT', label: 'Agreements', icon: '📄' },
        { value: 'RECEIPT', label: 'Receipts', icon: '🧾' },
        { value: 'POLICY', label: 'Policies', icon: '📋' },
        { value: 'STATEMENT', label: 'Statements', icon: '📊' },
        { value: 'TAX', label: 'Tax Documents', icon: '💼' },
        { value: 'INSURANCE', label: 'Insurance', icon: '🛡️' },
        { value: 'LOAN', label: 'Loan Documents', icon: '🏦' },
        { value: 'OTHER', label: 'Other', icon: '📎' }
    ];

    useEffect(() => {
        fetchData();
    }, []);

    useEffect(() => {
        filterDocuments();
    }, [documents, selectedCategory, searchQuery]);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [docsData, statsData] = await Promise.all([
                getUserDocuments(userId),
                getDocumentStats(userId).catch(() => null)
            ]);
            setDocuments(docsData);
            setStats(statsData);
        } catch (error) {
            console.error('Error fetching documents:', error);
        } finally {
            setLoading(false);
        }
    };

    const filterDocuments = () => {
        let filtered = documents;

        // Filter by category
        if (selectedCategory !== 'ALL') {
            filtered = filtered.filter(doc => doc.category === selectedCategory);
        }

        // Filter by search query
        if (searchQuery) {
            filtered = filtered.filter(doc => 
                doc.fileName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                doc.description?.toLowerCase().includes(searchQuery.toLowerCase())
            );
        }

        setFilteredDocuments(filtered);
    };

    const handleFileSelect = (event) => {
        const files = event.target.files;
        if (files.length > 0) {
            handleFileUpload(files[0]);
        }
    };

    const handleFileUpload = async (file) => {
        if (!file) return;

        try {
            setUploading(true);
            
            const formData = new FormData();
            formData.append('file', file);
            formData.append('userId', userId);
            formData.append('category', selectedCategory === 'ALL' ? 'OTHER' : selectedCategory);
            formData.append('description', '');

            await uploadDocument(formData);
            fetchData();
            
            // Reset file input
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        } catch (error) {
            alert('Error uploading file: ' + error.message);
        } finally {
            setUploading(false);
        }
    };

    const handleDrag = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === 'dragenter' || e.type === 'dragover') {
            setDragActive(true);
        } else if (e.type === 'dragleave') {
            setDragActive(false);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);
        
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFileUpload(e.dataTransfer.files[0]);
        }
    };

    const handleDownload = async (documentId, fileName) => {
        try {
            const blob = await downloadDocument(documentId);
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (error) {
            alert('Error downloading file: ' + error.message);
        }
    };

    const handleDelete = async (documentId) => {
        if (window.confirm('Are you sure you want to delete this document?')) {
            try {
                await deleteDocument(documentId);
                fetchData();
            } catch (error) {
                alert('Error deleting document: ' + error.message);
            }
        }
    };

    const formatFileSize = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    if (loading) {
        return <div className="loading-container">Loading documents...</div>;
    }

    return (
        <div className="documents-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-title">
                    <FaFolder />
                    <h1>Document Management</h1>
                </div>
                <div className="header-actions">
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileSelect}
                        style={{ display: 'none' }}
                    />
                    <button 
                        className="btn-upload"
                        onClick={() => fileInputRef.current?.click()}
                        disabled={uploading}
                    >
                        <FaPlus /> {uploading ? 'Uploading...' : 'Upload Document'}
                    </button>
                </div>
            </div>

            {/* Stats */}
            {stats && (
                <div className="stats-section">
                    <div className="stat-card">
                        <FaFileAlt />
                        <div className="stat-content">
                            <div className="stat-value">{stats.totalDocuments || 0}</div>
                            <div className="stat-label">Total Documents</div>
                        </div>
                    </div>
                    <div className="stat-card">
                        <FaCloudUploadAlt />
                        <div className="stat-content">
                            <div className="stat-value">{formatFileSize(stats.totalSize || 0)}</div>
                            <div className="stat-label">Total Storage</div>
                        </div>
                    </div>
                </div>
            )}

            {/* Search and Filter */}
            <div className="controls-section">
                <div className="search-box">
                    <FaSearch />
                    <input
                        type="text"
                        placeholder="Search documents..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
                <div className="filter-section">
                    <FaFilter />
                    <span>Filter:</span>
                    <select
                        value={selectedCategory}
                        onChange={(e) => setSelectedCategory(e.target.value)}
                    >
                        {categories.map(cat => (
                            <option key={cat.value} value={cat.value}>
                                {cat.icon} {cat.label}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* Upload Zone */}
            <div 
                className={`upload-zone ${dragActive ? 'drag-active' : ''}`}
                onDragEnter={handleDrag}
                onDragLeave={handleDrag}
                onDragOver={handleDrag}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
            >
                <FaCloudUploadAlt />
                <h3>Drag & Drop files here</h3>
                <p>or click to browse</p>
                <small>Supported: PDF, DOC, DOCX, XLS, XLSX, JPG, PNG (Max 10MB)</small>
            </div>

            {/* Documents Grid */}
            <div className="documents-section">
                <div className="section-header">
                    <h2>
                        {selectedCategory === 'ALL' ? 'All Documents' : 
                         categories.find(c => c.value === selectedCategory)?.label}
                    </h2>
                    <span className="count">{filteredDocuments.length} documents</span>
                </div>

                {filteredDocuments.length === 0 ? (
                    <div className="empty-state">
                        <div className="empty-icon">📂</div>
                        <h3>No documents found</h3>
                        <p>
                            {searchQuery ? 'Try a different search term' : 
                             'Upload your first document to get started'}
                        </p>
                    </div>
                ) : (
                    <div className="documents-grid">
                        {filteredDocuments.map(doc => (
                            <DocumentCard
                                key={doc.id}
                                document={doc}
                                onDownload={() => handleDownload(doc.id, doc.fileName)}
                                onDelete={() => handleDelete(doc.id)}
                                formatFileSize={formatFileSize}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Documents;
