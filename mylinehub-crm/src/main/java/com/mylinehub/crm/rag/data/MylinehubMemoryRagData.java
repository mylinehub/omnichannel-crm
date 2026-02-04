package com.mylinehub.crm.rag.data;


public class MylinehubMemoryRagData {

    // ======================================================================================
    // PART 3: PRICE / COST CALCULATION LOGIC, ALLOWED INTENTS, PRODUCT TYPES FOR MYLINEHUB
    // ======================================================================================
	public static final String MYLINEHUBCOSTCALCULATION =
	        "PART3: PRICE / COST CALCULATION LOGIC & ENUMS\n" +
	        "OVERVIEW: Follow exact pricing. All amounts in INR. Use params (seats, cloud/on-prem, users, IVR ch, GSM/PRI, GST, payment). Missing: ask user. 'ch'=channel.\n" +
	        "\n" +
	        "1) WhatsApp:\n" +
	        "   - Min recharge 500; outgoing 1/msg; incoming free.\n" +
	        "   - Total = outgoing msgs*1 + seat/setup fees.\n" +
	        "\n" +
	        "2) Telecom India (Cloud / Mobile / On-Prem):\n" +
	        "   2.1 Cloud:\n" +
	        "       - 2000/mo/seat (single ch).\n" +
	        "   2.2 Mobile:\n" +
	        "       - 3500/mo/seat (2 ch).\n" +
	        "   2.3 On-Prem (Fixed + Recurring):\n" +
	        "       Fixed:\n" +
	        "         - License: min 10 seats * 25000/seat.\n" +
	        "         - Router 8000.\n" +
	        "         - Servers 2*150000 (Asterisk physical).\n" +
	        "         - GSM Gateway 85000 each; compute by ports/concurrency.\n" +
	        "       Recurring:\n" +
	        "         - PRI: inbound 100/ch/mo, inbound+outbound 500/ch/mo.\n" +
	        "         - Asterisk: 1 server ~100 users.\n" +
	        "         - GSM: SIM 299/SIM.\n" +
	        "         - Internet 1000/mo (~100 users).\n" +
	        "         - AMC 20000/yr (5 tickets), 2000/extra session.\n" +
	        "   2.4 Payment:\n" +
	        "       - 1=1yr, 2=6mo, 3-5=3mo, >5 monthly; GST mandatory.\n" +
	        "\n" +
	        "3) AI Interview – Deep Technical Screening (Telephonic):\n" +
	        "   - Base price: INR 399 per interview (40 minutes).\n" +
	        "   - Current discount: Save INR 250, Pay only INR 149 per interview (as of now).\n" +
	        "   - Deep screening: AI asks role-specific, deep technical questions (not HR-style).\n" +
	        "   - Fireview: questions fired across multiple areas as per job description.\n" +
	        "   - Outreach: up to 3-5 attempts via WhatsApp, Email, and Call; then schedule interview.\n" +
	        "   - Billing: client pays only AFTER interview completion; otherwise client does not pay.\n" +
	        "   - Deliverables: call recording, transcription, AI analysis of results, and depth assessment.\n" +
	        "   - Note: This pricing covers telephonic interview only.\n" +
	        "   - Premium assisted interview: INR 1299 (Current discount, save INR 850: INR 449 per interview, AI agent joins Teams/Google Meet and behaves like interviewer).\n" +
	        "   - Premium checks: candidate video capture, government ID scan, and ensure no external help/teaching.\n" +
	        "   - More advanced verification features available in higher plans.\n" +
	        "\n" +
	        "CALC METHOD / TECHNIQUES FOR TELCOM (POINT 2 ABOVE):\n" +
	        "  a) Ask missing params.\n" +
	        "  b) Total = fixed + recurring + per-msg/per-ch; show steps.\n" +
	        "  c) Table summary (\\n, max3 cols).\n" +
	        "  d) Explain assumptions.\n" +
	        "\n" +
	        "EX: 100 users, telecom on-prem, 3 GSM:\n" +
	        "  - License 100*25000=2,500,000 (also ensure min 10 seats).\n" +
	        "  - Router 8000\n" +
	        "  - Servers 300000\n" +
	        "  - GSM 3*85000=255000\n" +
	        "  - Total fixed=3,063,000; recurring per rules.\n" +
	        "\n" +
	        "IVR: 1 ch ~3000 calls/mo; 100,000 calls/mo ~= 4 ch.\n" +
	        "\n" +
	        "ALLOWED INTENTS: PRICING, PRODUCT_INQUIRY, CATALOG_REQUEST, PAYMENT_QUERY, PASSWORD_RESET, AGENT_REQUEST, FOLLOW_UP, FUTURE_UPDATE, SUPPORT_TICKET, COMPLAINT, PERSONAL_MESSAGE, GROUP_FORWARD, BLOCK_USER, GREETING, GENERAL_QUERY, ORDER_STATUS, SCHEDULE_CALL, REFUND, RETURN, UNKNOWN\n" +
	        "PRODUCT TYPE ENUM: Whatsapp, Telecommunication, PhysicalProduct, OtherService\n" +
	        "END OF PART3.\n";

	 // ======================================================================================
	 // PART 5: VIDEO DEMOS AND PRODUCT INFORMATION (for Assistant Reference)
	 // ======================================================================================
	public static final String MYLINEHUBVIDEOS = "VIDEO DEMOS - Internal; include link in llmResponse if user asks features, wants CRM visualization, or is unsatisfied.\\nWeb Phone: https://youtu.be/cA_9uSfwsLg\\nFilestore: https://youtu.be/yTHPJmd_k-w\\nInstant Messaging: https://youtu.be/xE9GYn88sOM\\nMonitor Agents: https://youtu.be/gKCtp7R2Rbo\\nOnboard Employees: https://youtu.be/3C4KTQoD3Zk\\nGraphs & Reporting: https://youtu.be/b8iSfUXtP34\\nTelecom Features: https://youtu.be/dWhh2zw6pWY\\nTelecom CRM: https://youtu.be/7tzY9i2Qrlw\\nSchedule & Campaign: https://youtu.be/MAgmsUZE88E\\nWhatsApp CRM: https://youtube.com/shorts/QBV0zr9ytI0";


}
