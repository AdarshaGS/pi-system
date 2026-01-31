import { useState, useEffect } from 'react';
import { X, Plus } from 'lucide-react';
import { budgetApi } from '../api';
import './TagSelector.css';

const TagSelector = ({ selectedTags = [], onTagsChange, showManage = true }) => {
    const [availableTags, setAvailableTags] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        fetchTags();
    }, []);

    const fetchTags = async () => {
        try {
            const tags = await budgetApi.getUserTags();
            setAvailableTags(tags);
        } catch (error) {
            console.error('Failed to fetch tags:', error);
        }
    };

    const handleTagSelect = (tag) => {
        if (!selectedTags.find(t => t.id === tag.id)) {
            onTagsChange([...selectedTags, tag]);
        }
        setShowDropdown(false);
        setSearchTerm('');
    };

    const handleTagRemove = (tagId) => {
        onTagsChange(selectedTags.filter(t => t.id !== tagId));
    };

    const handleCreateNewTag = async () => {
        if (!searchTerm.trim()) return;

        try {
            // Generate random color
            const colors = ['#ef4444', '#f97316', '#f59e0b', '#84cc16', '#10b981', '#06b6d4', '#3b82f6', '#6366f1', '#8b5cf6', '#ec4899'];
            const randomColor = colors[Math.floor(Math.random() * colors.length)];

            const newTag = await budgetApi.createTag({
                name: searchTerm.trim(),
                color: randomColor
            });

            setAvailableTags([...availableTags, newTag]);
            handleTagSelect(newTag);
        } catch (error) {
            console.error('Failed to create tag:', error);
        }
    };

    const filteredTags = availableTags.filter(tag =>
        tag.name.toLowerCase().includes(searchTerm.toLowerCase()) &&
        !selectedTags.find(t => t.id === tag.id)
    );

    return (
        <div className="tag-selector">
            <div className="selected-tags">
                {selectedTags.map(tag => (
                    <span key={tag.id} className="tag-chip" style={{ backgroundColor: tag.color }}>
                        {tag.name}
                        <button
                            type="button"
                            className="tag-remove-btn"
                            onClick={() => handleTagRemove(tag.id)}
                        >
                            <X size={14} />
                        </button>
                    </span>
                ))}
                <button
                    type="button"
                    className="add-tag-btn"
                    onClick={() => setShowDropdown(!showDropdown)}
                >
                    <Plus size={16} />
                    Add Tag
                </button>
            </div>

            {showDropdown && (
                <div className="tag-dropdown">
                    <input
                        type="text"
                        className="tag-search"
                        placeholder="Search or create tag..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        autoFocus
                    />
                    <div className="tag-list">
                        {filteredTags.length === 0 && searchTerm.trim() && (
                            <button
                                type="button"
                                className="tag-option create-new"
                                onClick={handleCreateNewTag}
                            >
                                <Plus size={16} />
                                Create "{searchTerm}"
                            </button>
                        )}
                        {filteredTags.map(tag => (
                            <button
                                key={tag.id}
                                type="button"
                                className="tag-option"
                                onClick={() => handleTagSelect(tag)}
                            >
                                <span className="tag-color-indicator" style={{ backgroundColor: tag.color }} />
                                {tag.name}
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default TagSelector;
