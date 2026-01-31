/**
 * Utility function to download files from blob data
 * @param {Blob} blob - The blob data to download
 * @param {string} filename - The name of the file
 */
export const downloadFile = (blob, filename) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.parentNode.removeChild(link);
    window.URL.revokeObjectURL(url);
};

/**
 * Generate filename with current date
 * @param {string} prefix - File prefix (e.g., 'expenses', 'incomes', 'report')
 * @param {string} extension - File extension (e.g., 'csv', 'xlsx', 'pdf')
 * @returns {string} - Generated filename
 */
export const generateFilename = (prefix, extension) => {
    const date = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
    return `${prefix}_${date}.${extension}`;
};
