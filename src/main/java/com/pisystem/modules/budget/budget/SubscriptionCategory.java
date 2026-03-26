package com.budget;

/**
 * Enum representing subscription categories
 */
public enum SubscriptionCategory {
    ENTERTAINMENT("Entertainment"), // Netflix, Spotify, Disney+
    SOFTWARE("Software"), // Adobe, Microsoft 365, GitHub
    CLOUD_STORAGE("Cloud Storage"), // Google Drive, Dropbox, iCloud
    NEWS_MEDIA("News & Media"), // Newspapers, Magazines
    FITNESS("Fitness"), // Gym, Yoga apps, Fitness apps
    EDUCATION("Education"), // Online courses, Learning platforms
    GAMING("Gaming"), // PlayStation Plus, Xbox Game Pass
    UTILITIES("Utilities"), // Internet, Mobile plans
    FOOD_DELIVERY("Food Delivery"), // Zomato Gold, Swiggy One
    SHOPPING("Shopping"), // Amazon Prime, Flipkart Plus
    OTHER("Other");

    private final String displayName;

    SubscriptionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
