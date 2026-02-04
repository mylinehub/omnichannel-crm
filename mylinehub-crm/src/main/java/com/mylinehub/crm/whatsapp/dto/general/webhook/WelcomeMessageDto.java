package com.mylinehub.crm.whatsapp.dto.general.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.whatsapp.dto.general.contact.UrlDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WelcomeMessageDto {
	    private String text;             // The main message text or greeting
	    private String headline;         // Optional headline/title
	    private String body;             // Optional full body text if separate from text
	    private String media_type;       // e.g., "image", "video", etc.
	    private String image_url;        // URL if image media
	    private String video_url;        // URL if video media
	    private String thumbnail_url;    // URL for a thumbnail version if present
	    private String ctwa_clid;        // Click to WhatsApp click ID (in referral context) :contentReference[oaicite:2]{index=2}
	    // Add more optional fields as needed based on additional payload data
}