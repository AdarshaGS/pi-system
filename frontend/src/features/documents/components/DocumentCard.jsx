/**
 * DocumentCard Component
 * 
 * Displays a single document with:
 * - File icon based on type
 * - File name and metadata
 * - Category badge
 * - Download and delete actions
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import React from 'react';
import {
    FaFilePdf,
    FaFileWord,
    FaFileExcel,
    FaFileImage,
    FaFile,
    FaDownload,
    FaTrash,
    FaCalendarAlt
} from 'react-icons/fa';
import './DocumentCard.css';

const DocumentCard = ({ document, onDownload, onDelete, formatFileSize }) => {
    
    const getFileIcon = (fileName, mimeType) => {
        const extension = fileName.split('.').pop().toLowerCase();
        
        if (mimeType?.includes('pdf') || extension === 'pdf') {
            return <FaFilePdf className="file-icon pdf" />;
        }
        if (mimeType?.includes('word') || ['doc', 'docx'].includes(extension)) {
            return <FaFileWord className="file-icon word" />;
        }
        if (mimeType?.includes('excel') || mimeType?.includes('spreadsheet') || 
            ['xls', 'xlsx'].includes(extension)) {
            return <FaFileExcel className="file-icon excel" />;
        }
        if (mimeType?.includes('image') || ['jpg', 'jpeg', 'png', 'gif'].includes(extension)) {
            return <FaFileImage className="file-icon image" />;
        }
        return <FaFile className="file-icon default" />;
    };

    const getCategoryColor = (category) => {
        const colors = {
            'AGREEMENT': '#6366f1',
            'RECEIPT': '#10b981',
            'POLICY': '#3b82f6',
            'STATEMENT': '#8b5cf6',
            'TAX': '#ef4444',
            'INSURANCE': '#f59e0b',
            'LOAN': '#ec4899',
            'OTHER': '#6b7280'
        };
        return colors[category] || '#6b7280';
    };

    const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    return (
        <div className="document-card">
            {/* File Icon */}
            <div className="document-icon-container">
                {getFileIcon(document.fileName, document.mimeType)}
            </div>

            {/* Document Info */}
            <div className="document-info">
                <h3 className="document-name" title={document.fileName}>
                    {document.fileName}
                </h3>
                
                <div className="document-meta">
                    <span 
                        className="category-badge"
                        style={{ 
                            backgroundColor: getCategoryColor(document.category) + '20',
                            color: getCategoryColor(document.category)
                        }}
                    >
                        {document.category}
                    </span>
                    <span className="file-size">{formatFileSize(document.fileSize)}</span>
                </div>

                {document.description && (
                    <p className="document-description">{document.description}</p>
                )}

                <div className="document-date">
                    <FaCalendarAlt />
                    <span>Uploaded: {formatDate(document.uploadedAt)}</span>
                </div>
            </div>

            {/* Actions */}
            <div className="document-actions">
                <button 
                    className="btn-action btn-download"
                    onClick={onDownload}
                    title="Download"
                >
                    <FaDownload />
                </button>
                <button 
                    className="btn-action btn-delete"
                    onClick={onDelete}
                    title="Delete"
                >
                    <FaTrash />
                </button>
            </div>
        </div>
    );
};

export default DocumentCard;
