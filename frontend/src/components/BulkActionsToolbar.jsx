import { Trash2, Tag, X } from 'lucide-react';
import './BulkActionsToolbar.css';

const BulkActionsToolbar = ({ 
    selectedCount, 
    onDelete, 
    onUpdateCategory, 
    onClear 
}) => {
    if (selectedCount === 0) return null;

    return (
        <div className="bulk-toolbar">
            <div className="bulk-info">
                <span className="count-badge">{selectedCount}</span>
                <span>item{selectedCount > 1 ? 's' : ''} selected</span>
            </div>
            
            <div className="bulk-actions">
                <button className="bulk-btn" onClick={onUpdateCategory}>
                    <Tag size={16} />
                    Change Category
                </button>
                <button className="bulk-btn danger" onClick={onDelete}>
                    <Trash2 size={16} />
                    Delete Selected
                </button>
                <button className="clear-btn" onClick={onClear} title="Clear selection">
                    <X size={20} />
                </button>
            </div>
        </div>
    );
};

export default BulkActionsToolbar;
