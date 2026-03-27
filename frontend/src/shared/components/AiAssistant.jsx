import React, { useState, useRef, useEffect } from 'react';
import apiClient from '@/core/api';
import {
    MessageSquare,
    X,
    Send,
    Bot,
    Loader2
} from 'lucide-react';
import './AiAssistant.css';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

const AiAssistant = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [message, setMessage] = useState('');
    const [messages, setMessages] = useState([
        {
            role: 'assistant',
            text: "Hello! I'm Pi-Assistant, your personal financial architect. How can I help you optimize your wealth structure today?"
        }
    ]);
    const [isLoading, setIsLoading] = useState(false);
    const scrollRef = useRef(null);

    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages]);

    const handleSend = async () => {
        if (!message.trim() || isLoading) return;

        const userMsg = { role: 'user', text: message };
        setMessages(prev => [...prev, userMsg]);
        setMessage('');
        setIsLoading(true);

        try {
            const response = await apiClient.post('/v1/ai/chat', { message: userMsg.text });
            setMessages(prev => [...prev, { role: 'assistant', text: response.response }]);
        } catch (error) {
            console.error('AI Assistant Error:', error);
            const errorMsg = error.response?.data?.message || error.message || "Unknown error";
            setMessages(prev => [...prev, {
                role: 'assistant',
                text: "I apologize, but I'm having trouble analyzing your financial snapshot at the moment. Error: " + errorMsg
            }]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="ai-assistant-container">
            {/* Floating Action Button */}
            {!isOpen && (
                <button className="ai-fab" onClick={() => setIsOpen(true)}>
                    <MessageSquare size={28} />
                </button>
            )}

            {/* Chat Window */}
            {isOpen && (
                <div className="ai-chat-window">
                    {/* Header */}
                    <div className="ai-header">
                        <div className="ai-header-info">
                            <div className="ai-avatar">
                                <Bot size={24} />
                            </div>
                            <div className="ai-title">
                                <h3>Pi-Assistant</h3>
                                <span>Financial Architect</span>
                            </div>
                        </div>
                        <button className="close-btn" onClick={() => setIsOpen(false)}>
                            <X size={20} />
                        </button>
                    </div>

                    {/* Messages Area */}
                    <div className="ai-messages" ref={scrollRef}>
                        {messages.map((msg, i) => (
                            <div
                                key={i}
                                className={`ai-message ${msg.role}`}
                            >
                                {msg.role === 'assistant' ? (
                                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                                        {msg.text}
                                    </ReactMarkdown>
                                ) : (
                                    msg.text
                                )}
                            </div>
                        ))}
                        {isLoading && (
                            <div className="ai-loading">
                                <Loader2 size={16} className="animate-spin" />
                                <span>Analysing your financial snapshot...</span>
                            </div>
                        )}
                    </div>

                    {/* Input Area */}
                    <div className="ai-input-area">
                        <input
                            type="text"
                            className="ai-input"
                            placeholder="Ask about your financial health..."
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                            autoFocus
                        />
                        <button
                            className="send-btn"
                            onClick={handleSend}
                            disabled={!message.trim() || isLoading}
                        >
                            <Send size={18} />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AiAssistant;
