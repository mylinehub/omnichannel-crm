package com.mylinehub.voicebridge.agi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;

/**
 * Minimal AGI example to demonstrate a fallback call flow when ARI is unavailable.
 *
 * <p>Behavior:
 * <ol>
 *   <li>Answers the call.</li>
 *   <li>Plays the system recording {@code beep}.</li>
 *   <li>Speaks the letters {@code HELLO}.</li>
 *   <li>Hangs up.</li>
 * </ol>
 *
 * @apiNote This is intentionally simple; it proves the toolchain and dialplan wiring.
 * @implNote Keep prompts short and ensure referenced system recordings exist on Asterisk.
 */
public class MinimalAgiExample implements AgiScript {

  /**
   * Execute the minimal AGI script.
   *
   * @param request the AGI request metadata
   * @param channel the active AGI channel
   * @throws AgiException if an AGI operation fails
   */
  @Override
  public void service(AgiRequest request, AgiChannel channel) throws AgiException {
    // Answer the incoming call.
    channel.answer();

    // Play the built-in/system recording named "beep" (no file extension).
    channel.streamFile("beep");

    // Speak an alphanumeric string; here we spell out "HELLO".
    channel.sayAlpha("9711761156");

    // Hang up the call.
    channel.hangup();
  }
}
