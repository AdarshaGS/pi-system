package com.investments.stocks.networth.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntityTemplateDTO {
    private String type;
    private String displayName;
    private String description;
    private String category; // e.g. "Cash", "Investment", "Loan"
}
