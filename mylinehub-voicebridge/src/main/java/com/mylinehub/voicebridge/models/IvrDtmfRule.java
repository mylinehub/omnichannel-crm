package com.mylinehub.voicebridge.models;

import lombok.Data;

import java.util.List;

/**
 * Maps a DTMF digit to meaning + next action.
 *
 * digit: pressed key (e.g. "1", "2", "5")
 * interest: meaning (e.g. "Pharmacy", "Pathology", "Talk to senior")
 * action:
 *   - "REPLAY_CUT"  : play confirmation recording, then hangup
 *   - "REDIRECT"    : redirect to one of the numbers in redirectNumberList
 *   - "NONE"        : do nothing (placeholder)
 *
 * redirectNumberList:
 *   - optional list of numbers to redirect/hunt-group
 *   - first number is used initially (you can later do round-robin)
 */
@Data
public class IvrDtmfRule {
  private String dtmfPressed;
  private String interest;
  private String action;
  private List<String> redirectNumberList;
}
