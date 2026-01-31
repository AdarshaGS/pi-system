import { useState, useEffect } from 'react';
import { X, Edit2, Trash2, Plus, Save } from 'lucide-react';
import { budgetApi } from '../api';
import './TagManagementModal.css';

const TagManagementModal = ({ show, onClose }) => {
    const [tags, setTags] = useState([]);
    const [editingTag, setEditingTag] = useState(null);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [formData, setFormData] = useState({ name: '', color: '#6366f1' });
    const [loading, setLoading] = useState(false);

    const colorOptions = [
        { name: 'Red', value: '#ef4444' },
        { name: 'Orange', value: '#f97316' },
        { name: 'Amber', value: '#f59e0b' },
        { name: 'Yellow', value: '#eab308' },
        { name: 'Lime', value: '#84cc16' },
        { name: 'Green', value: '#10b981' },
        { name: 'Cyan', value: '#06b6d4' },
        { name: 'Blue', value: '#3b82f6' },
        { name: 'Indigo', value: '#6366f1' },
        { name: 'Purple', value: '#8b5cf6' },
        { name: 'Pink', value: '#ec4899' },
        { name: 'Rose', value: '#f43f5e' }
    ];

    useEffect(() => {
        if (show) {
            fetchTags();
        }
    }, [show]);

    const fetchTags = async () => {
        setLoading(true);
        try {
            const fetchedTags = await budgetApi.getUserTags();
            setTags(fetchedTags);
        } catch (error) {
            console.error('Failed to fetch tags:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = async () => {
        if (!formData.name.trim()) return;

        try {
            const newTag = await budgetApi.createTag(formData);
            setTags([...tags, newTag]);
            resetForm();
        } catch (error) {
            console.error('Failed to create tag:', error);
            alert('Failed to create tag');
        }
    };

    const handleUpdate = async () => {
        if (!formData.name.trim()) return;

        try {
            const updatedTag = await budgetApi.updateTag(editingTag.id, formData);
            setTags(tags.map(t => t.id === editingTag.id ? updatedTag : t));
            resetForm();
        } catch (error) {
            console.error('Failed to update tag:', error);
            alert('Failed to update tag');
        }
    };

    const handleDelete = async (tagId) => {
        if (!window.confirm('Delete this tag? It will be removed from all expenses.')) {
            return;
        }

        try {
            await budgetApi.deleteTag(tagId);
            setTags(tags.filter(t => t.id !== tagId));
        } catch (error) {
            console.error('Failed to delete tag:', error);
            alert('Failed to delete tag');
        }
    };

    const startEdit = (tag) => {
        setEditingTag(tag);
        setFormData({ name: tag.name, color: tag.color });
        setShowCreateForm(false);
    };

    const resetForm = () => {
        setEditingTag(null);
        setShowCreateForm(false);
        setFormData({ name: '', color: '#6366f1' });
    };

    if (!show) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content tag-modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Manage Tags</h2>
                    <button className="close-btn" onClick={onClose}>
                        <X size={24} />
                    </button>
                </div>

                <div className="modal-body">
                    {/* Create/Edit Form */}
                    {(showCreateForm || editingTag) && (
                        <div className="tag-form">
                            <h3>{editingTag ? 'Edit Tag' : 'Create New Tag'}</h3>
                            <div className="form-row">
                                <input
                                    type="text"
                                    placeholder="Tag name"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    maxLength={30}
                                />
                            </div>
                            <div className="color-picker">
                                <label>Color:</label>
                                <div className="color-options">
                                    {colorOptions.map(color => (
                                        <button
                                            key={color.value}
                                            type="button"
                                            className={`color-option ${formData.color === color.value ? 'selected' : ''}`}
                                            style={{ backgroundColor: color.value }}
                                            onClick={() => setFormData({ ...formData, color: color.value })}
                                            title={color.name}
                                        />
                                    ))}
                                </div>
                            </div>
                            <div className="form-actions">
                                <button className="btn-secondary" onClick={resetForm}>
                                    Cancel
                                </button>
                                <button 
                                    className="btn-primary" 
                                    onClick={editingTag ? handleUpdate : handleCreate}
                                    disabled={!formData.name.trim()}
                                >
                                    <Save size={16} />
                                    {editingTag ? 'Update' : 'Create'}
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Add New Button */}
                    {!showCreateForm && !editingTag && (
                        <button className="btn-create-tag" onClick={() => setShowCreateForm(true)}>
                            <Plus size={16} />
                            Create New Tag
                        </button>
                    )}

                    {/* Tags List */}
                    <div className="tags-list">
                        {loading ? (
                            <div className="loading-text">Loading tags...</div>
                        ) : tags.length === 0 ? (
                            <div className="empty-tags">
                                <p>No tags yet. Create one to get started!</p>
                            </div>
                        ) : (
                            tags.map(tag => (
                                <div key={tag.id} className="tag-item">
                                    <div className="tag-info">
                                        <span className="tag-color-box" style={{ backgroundColor: tag.color }} />
                                        <span className="tag-name">{tag.name}</span>
                                    </div>
                                    <div className="tag-actions">
                                        <button className="icon-btn" onClick={() => startEdit(tag)}>
                                            <Edit2 size={16} />
                                        </button>
                                        <button className="icon-btn danger" onClick={() => handleDelete(tag.id)}>
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TagManagementModal;
