package com.example.data

import com.example.model.DrillDifficulty
import com.example.model.DrillQuestion
import com.example.model.TacticalTier
import com.example.model.ThreatSeverity

object SecurityPlusRepository {

    fun getInitialTacticalDeck(): List<DrillQuestion> = listOf(
        // ==================== EASY // RECON BASELINE (100 PTS) ====================
        DrillQuestion(
            vectorId = "VEC-SY0-701-011E",
            domainCode = "1.0",
            domainTitle = "1.0 GENERAL SECURITY CONCEPTS",
            severity = ThreatSeverity.ELEVATED,
            difficulty = DrillDifficulty.EASY,
            tier = TacticalTier.TIER_1,
            question = "A system administrator configures a secure file distribution server so that every downloadable firmware package is accompanied by a SHA-256 digest signed with the organization's private key. Which two pillars of information security does this control directly enforce?",
            options = listOf(
                "Integrity and Non-repudiation",
                "Confidentiality and Availability",
                "Obfuscation and High Availability",
                "Authorization and Network Accounting"
            ),
            correctIndex = 0,
            rationale = "[BASELINE CONTROL VERIFIED]: Hashing the firmware package with SHA-256 verifies data Integrity (detecting any bit-level tampering), while signing that hash with the organization's asymmetric private key provides Non-repudiation and origin authenticity. Encryption is not applied to the payload itself, so Confidentiality is not enforced.",
            commandReference = "sha256sum -c firmware_v4.2.sig --verify-pubkey corp_ signing.pub"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-022E",
            domainCode = "2.0",
            domainTitle = "2.0 THREATS, VULNERABILITIES & MITIGATIONS",
            severity = ThreatSeverity.HIGH,
            difficulty = DrillDifficulty.EASY,
            tier = TacticalTier.TIER_1,
            question = "An attacker sends a highly targeted SMS message to the Chief Financial Officer claiming that a corporate wire transfer requires immediate verification via a shortened link (`https://auth-pay-corp.example.co`). What specific social engineering vector is being executed?",
            options = listOf(
                "Smishing combined with Spear Phishing (Whaling)",
                "Vishing combined with Pharming DNS poisoning",
                "Shoulder surfing with RFID badge cloning",
                "Dumpster diving and tailgating into the data center"
            ),
            correctIndex = 0,
            rationale = "[THREAT VECTOR VERIFIED]: Phishing conducted over SMS text messaging is known as Smishing. Because the message specifically targets a C-suite executive (the CFO) with tailored financial context, it also qualifies as Whaling (executive Spear Phishing).",
            commandReference = "url-sandbox --analyze 'https://auth-pay-corp.example.co' --inspect-tls"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-033E",
            domainCode = "3.0",
            domainTitle = "3.0 SECURITY ARCHITECTURE",
            severity = ThreatSeverity.ELEVATED,
            difficulty = DrillDifficulty.EASY,
            tier = TacticalTier.TIER_1,
            question = "A network engineer wants to place a public-facing HTTPS reverse proxy in a buffered network zone that sits between the untrusted public internet and the internal corporate LAN, separated by dual stateful firewalls. What is this architectural zone called?",
            options = listOf(
                "Screened Subnet (DMZ / Demilitarized Zone)",
                "Air-Gapped SCADA Enclave",
                "Loopback Management Plane",
                "Implicit Trust Core Backbone"
            ),
            correctIndex = 0,
            rationale = "[ARCHITECTURE VERIFIED]: A Screened Subnet (commonly referred to as a DMZ) isolates public-facing services (such as web, mail, or reverse proxy servers) between an external perimeter firewall and an internal firewall so a compromise of the proxy does not grant direct Layer-2/Layer-3 access to the internal LAN.",
            commandReference = "ip route show table dmz_screened_subnet"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-044E",
            domainCode = "4.0",
            domainTitle = "4.0 SECURITY OPERATIONS",
            severity = ThreatSeverity.ELEVATED,
            difficulty = DrillDifficulty.EASY,
            tier = TacticalTier.TIER_1,
            question = "A junior SOC analyst needs to block inbound unencrypted Telnet and FTP command traffic at the perimeter firewall while permitting secure SSH and HTTPS administration. Which port rule set should be configured?",
            options = listOf(
                "DENY TCP/23 & TCP/21; ALLOW TCP/22 & TCP/443",
                "DENY TCP/22 & TCP/443; ALLOW TCP/23 & TCP/21",
                "DENY UDP/53 & TCP/3389; ALLOW TCP/80 & TCP/110",
                "DENY TCP/389 & UDP/161; ALLOW TCP/25 & TCP/143"
            ),
            correctIndex = 0,
            rationale = "[PORT TELEMETRY VERIFIED]: Telnet operates on TCP/23 and FTP control operates on TCP/21 (both transmit credentials in cleartext). SSH operates on TCP/22 and HTTPS operates over TLS on TCP/443.",
            commandReference = "ufw deny 21/tcp && ufw deny 23/tcp && ufw allow 22/tcp && ufw allow 443/tcp"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-055E",
            domainCode = "5.0",
            domainTitle = "5.0 PROGRAM MANAGEMENT & OVERSIGHT",
            severity = ThreatSeverity.ELEVATED,
            difficulty = DrillDifficulty.EASY,
            tier = TacticalTier.TIER_1,
            question = "Before two organizations begin sharing proprietary threat intelligence feeds, their legal teams sign a formal document that strictly prohibits either party from disclosing confidential technical specifications to external entities. Which agreement defines this obligation?",
            options = listOf(
                "Non-Disclosure Agreement (NDA)",
                "Service Level Agreement (SLA)",
                "Interconnection Security Agreement (ISA)",
                "Memorandum of Understanding (MOU)"
            ),
            correctIndex = 0,
            rationale = "[GOVERNANCE VERIFIED]: A Non-Disclosure Agreement (NDA) is a legally binding contract establishing confidentiality obligations between parties sharing sensitive or proprietary data. An SLA defines uptime/performance metrics, and an ISA defines technical connection parameters.",
            commandReference = "compliance-cli --verify-contract NDA-2026-CORP --status active"
        ),

        // ==================== MEDIUM // HARDENED OPS (200 PTS) ====================
        DrillQuestion(
            vectorId = "VEC-SY0-701-109F",
            domainCode = "1.0",
            domainTitle = "1.0 GENERAL SECURITY CONCEPTS",
            severity = ThreatSeverity.HIGH,
            difficulty = DrillDifficulty.MEDIUM,
            tier = TacticalTier.TIER_1,
            question = "An enterprise PKI architect wants to ensure that if a web server's RSA private key is compromised six months from now, an adversary who passively captured encrypted PCAP traffic today cannot decrypt the historical TLS sessions. Furthermore, browsers must receive signed certificate revocation status stapled directly during the TLS handshake without querying an external third-party responder URL. Which configuration satisfies BOTH mandates?",
            options = listOf(
                "Ephemeral Diffie-Hellman key exchange (ECDHE) for Perfect Forward Secrecy (PFS) + OCSP Stapling",
                "Static RSA key transport + Certificate Revocation List (CRL) HTTP distribution points",
                "Pre-Shared Key (PSK) symmetric cipher suite + DNS CAA record enforcement",
                "Wildcard X.509 certificates with 10-year validity + HTTP Strict Transport Security (HSTS)"
            ),
            correctIndex = 0,
            rationale = "[CRYPTOGRAPHIC VERIFICATION]: Ephemeral Diffie-Hellman (DHE/ECDHE) generates unique, session-specific keys that are never transmitted or derived solely from the long-term private key, guaranteeing Perfect Forward Secrecy (PFS). OCSP Stapling binds a time-stamped, CA-signed OCSP response directly into the TLS handshake, eliminating client privacy leaks and latency to external OCSP responders.",
            commandReference = "openssl s_client -connect api.tactical.mil:443 -status -tls1_3"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-208M",
            domainCode = "2.0",
            domainTitle = "2.0 THREATS, VULNERABILITIES & MITIGATIONS",
            severity = ThreatSeverity.HIGH,
            difficulty = DrillDifficulty.MEDIUM,
            tier = TacticalTier.TIER_1,
            question = "During a web application penetration test, an auditor notices that modifying the URL parameter `?invoice_id=1048` to `?invoice_id=1049` returns another customer's sensitive billing PDF without re-verifying object-level ownership for the authenticated session token. Which vulnerability class has been identified?",
            options = listOf(
                "Insecure Direct Object Reference (IDOR) / Broken Object Level Authorization",
                "Server-Side Request Forgery (SSRF) via metadata loopback",
                "DOM-based Cross-Site Scripting (XSS) payload reflection",
                "XML External Entity (XXE) out-of-band file disclosure"
            ),
            correctIndex = 0,
            rationale = "[VULNERABILITY VERIFIED]: Insecure Direct Object Reference (IDOR) occurs when an application exposes a direct reference to an internal implementation object (such as a sequential database key `invoice_id`) without performing an authorization check to confirm the authenticated user owns that specific record.",
            commandReference = "ffuf -u https://portal.corp/api/invoice?invoice_id=FUZZ -w seq_ids.txt -mc 200"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-308C",
            domainCode = "3.0",
            domainTitle = "3.0 SECURITY ARCHITECTURE",
            severity = ThreatSeverity.HIGH,
            difficulty = DrillDifficulty.MEDIUM,
            tier = TacticalTier.TIER_1,
            question = "An industrial water treatment facility operates legacy Programmable Logic Controllers (PLCs) using unauthenticated Modbus/TCP over a flat operational network. The Chief Information Security Officer (CISO) mandates immediate isolation of the SCADA supervisory nodes from the corporate IT VLAN without replacing the fragile legacy PLCs, which crash when scanned. Which architectural control BEST achieves this requirement?",
            options = listOf(
                "Deploy a Purdue Model industrial DMZ with unidirectional security gateways (data diodes) and jump hosts",
                "Configure active vulnerability scanning every 15 minutes across the PLC subnet with automated patching",
                "Install full-disk BitLocker encryption and UEFI Secure Boot directly onto the legacy PLC firmware",
                "Bridge the SCADA VLAN directly to the public cloud VPC using BGP anycast routing"
            ),
            correctIndex = 0,
            rationale = "[ARCHITECTURE VERIFIED]: ICS/SCADA environments with fragile legacy PLCs rely on network segmentation following the Purdue Enterprise Reference Architecture (ISA-95/ISA-99). Implementing an Industrial DMZ, jump servers, and unidirectional data diodes prevents corporate IT lateral movement into OT zones without touching or actively scanning legacy PLC hardware.",
            commandReference = "nft add rule inet ot_filter forward ip saddr @corp_it ip daddr @plc_zone drop"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-412D",
            domainCode = "4.0",
            domainTitle = "4.0 SECURITY OPERATIONS",
            severity = ThreatSeverity.CRITICAL,
            difficulty = DrillDifficulty.MEDIUM,
            tier = TacticalTier.TIER_1,
            question = "During an active ransomware containment operation, the Incident Response Lead needs to preserve volatile evidence from a compromised Linux Kubernetes worker node before isolating it from the cluster fabric. In accordance with RFC 3227 Order of Volatility, which sequence of collection from MOST volatile to LEAST volatile must the responder execute?",
            options = listOf(
                "CPU registers & L1/L2 cache -> Kernel routing tables & active RAM -> Ephemeral container `/tmp` storage -> Remote archival syslog snapshots",
                "Remote archival syslog snapshots -> NVMe disk image -> Active RAM -> CPU registers & L1/L2 cache",
                "NVMe full disk clone -> Active network socket table -> CPU cache -> Offline tape backup",
                "Container image registry layers -> `/var/log/audit/audit.log` -> Process memory map -> CPU registers"
            ),
            correctIndex = 0,
            rationale = "[ORDER OF VOLATILITY VERIFIED]: Per RFC 3227 digital forensics standards, evidence must be collected from most volatile to least volatile: (1) CPU registers and cache (nanoseconds), (2) Routing tables, ARP cache, process table, and physical RAM, (3) Temporary file systems / local persistent storage, and (4) Remote logging and archival backups.",
            commandReference = "insmod lime-5.15.ko 'path=/mnt/forensics/node09.lime format=lime'"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-503E",
            domainCode = "5.0",
            domainTitle = "5.0 PROGRAM MANAGEMENT & OVERSIGHT",
            severity = ThreatSeverity.ELEVATED,
            difficulty = DrillDifficulty.MEDIUM,
            tier = TacticalTier.TIER_1,
            question = "A financial SaaS provider processes credit card transactions worth $40M annually. Quantitative risk analysis determines that a critical SQL injection vulnerability in its payment gateway has an Asset Value (AV) of $800,000, an Exposure Factor (EF) of 45%, and an Annualized Rate of Occurrence (ARO) of 0.50. A Web Application Firewall (WAF) tuning contract costs $45,000/year and reduces the ARO to 0.05. What is the initial Annualized Loss Expectancy (ALE) prior to mitigation?",
            options = listOf(
                "$180,000 (SLE = $360,000; ALE = $360,000 x 0.50)",
                "$360,000 (SLE = $800,000; ALE = $800,000 x 0.45)",
                "$400,000 (SLE = $800,000; ALE = $800,000 x 0.50)",
                "$18,000 (SLE = $360,000; ALE = $360,000 x 0.05)"
            ),
            correctIndex = 0,
            rationale = "[RISK TELEMETRY VERIFIED]: Single Loss Expectancy (SLE) = Asset Value ($800,000) x Exposure Factor (0.45) = $360,000. Annualized Loss Expectancy (ALE) = SLE ($360,000) x Annualized Rate of Occurrence (0.50) = $180,000. With the WAF reducing ARO to 0.05, post-mitigation ALE drops to $18,000, saving $162,000/year against a $45,000 control cost.",
            commandReference = "risk-calc --av 800000 --ef 0.45 --aro 0.50 --verify-ale"
        ),

        // ==================== HARD // ADVERSARY SIM (350 PTS) ====================
        DrillQuestion(
            vectorId = "VEC-SY0-701-101A",
            domainCode = "1.0",
            domainTitle = "1.0 GENERAL SECURITY CONCEPTS",
            severity = ThreatSeverity.CRITICAL,
            difficulty = DrillDifficulty.HARD,
            tier = TacticalTier.TIER_1,
            question = "A defense contractor is migrating its legacy VPN perimeter to a Zero Trust Architecture (ZTA). During an active session, a developer's endpoint telemetry reports that the local EDR daemon was unexpectedly suspended, and the Policy Engine (PE) immediately signals the Policy Enforcement Point (PEP) to terminate the micro-segmented tunnel. Which core Zero Trust component is responsible for combining continuous threat intelligence, behavioral heuristics, and PKI posture before issuing the session revocation token?",
            options = listOf(
                "Control Plane Policy Administrator (PA) operating alongside the Policy Engine (PE)",
                "Data Plane Implicit Trust Zone Broker",
                "Split-Tunnel RADIUS Accounting Concentrator",
                "Perimeter Next-Generation Firewall (NGFW) Stateful Inspection Table"
            ),
            correctIndex = 0,
            rationale = "[CONTROL PLANE VERIFIED]: Under NIST SP 800-207 Zero Trust Architecture, the Control Plane consists of the Policy Engine (PE) and Policy Administrator (PA) (collectively the Policy Decision Point, PDP). The PE evaluates continuous posture telemetry, threat intelligence, and access policies, while the PA establishes or revokes the communication path via the Policy Enforcement Point (PEP) in the Data Plane.",
            commandReference = "zt-ctl policy evaluate --subject dev-node-04 --posture-check edr_active"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-204B",
            domainCode = "2.0",
            domainTitle = "2.0 THREATS, VULNERABILITIES & MITIGATIONS",
            severity = ThreatSeverity.CRITICAL,
            difficulty = DrillDifficulty.HARD,
            tier = TacticalTier.TIER_1,
            question = "SOC analysts investigating an alert on a domain controller discover an adversary executed a malicious binary signed with a valid, non-expired code-signing certificate belonging to a third-party hardware vendor. Further memory forensics reveals the attacker injected a malicious DLL into a legitimate `svchost.exe` process via process hollowing. Which combination of attack vectors is the SOC observing?",
            options = listOf(
                "Supply chain compromise coupled with privilege escalation / defense evasion via process injection",
                "Pass-the-Hash (PtH) replay combined with ARP cache poisoning",
                "Watering hole attack utilizing Cross-Site Request Forgery (CSRF) token replay",
                "Evil Twin 802.11 deauthentication frame flood with downgrade negotiation"
            ),
            correctIndex = 0,
            rationale = "[FORENSIC MATCH]: A valid vendor-signed malicious binary indicates a supply chain or vendor private-key compromise. Unmapping legitimate code from `svchost.exe` and replacing it with malicious payloads in memory is the textbook definition of Process Hollowing, a defense-evasion and privilege-escalation technique (MITRE ATT&CK T1055.012) designed to bypass standard allowlists.",
            commandReference = "volatility -f memdump.raw windows.malfind --pid 1184"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-215G",
            domainCode = "2.0",
            domainTitle = "2.0 THREATS, VULNERABILITIES & MITIGATIONS",
            severity = ThreatSeverity.CRITICAL,
            difficulty = DrillDifficulty.HARD,
            tier = TacticalTier.TIER_2,
            question = "[TIER-2 ZERO-DAY DRILL] Threat hunters detect abnormal outbound DNS TXT queries (`a8f9d01c.c2-telemetry.example.net`) originating from an air-gapped VLAN's internal recursive resolver every 45 seconds. Packet inspection reveals high Shannon entropy in subdomain labels and irregular payload sizes, though port 80/443 egress is strictly blocked at the perimeter firewall. Which mitigation directly neutralizes this exfiltration channel?",
            options = listOf(
                "Enforce DNS-over-HTTPS to external public resolvers on all internal endpoints",
                "Implement Response Policy Zones (RPZ), entropy-based DNS tunneling inspection, and restrict recursive queries to authorized internal forwarders",
                "Enable DNSSEC validation on the external authoritative zone only",
                "Increase the Time-To-Live (TTL) on internal PTR records to 86,400 seconds"
            ),
            correctIndex = 1,
            rationale = "[EXFILTRATION MITIGATED]: The high-entropy subdomain labels and periodic TXT lookups indicate DNS Tunneling (C2 and data exfiltration over UDP/53). Deploying DNS Firewall / Response Policy Zones (RPZ) alongside behavioral/entropy inspection of query labels and blocking unauthorized recursive resolution stops C2 payloads encapsulated in DNS queries.",
            commandReference = "suricata-cli --inspect-dns-entropy --threshold 4.2 --drop-txt-c2"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-322H",
            domainCode = "3.0",
            domainTitle = "3.0 SECURITY ARCHITECTURE",
            severity = ThreatSeverity.CRITICAL,
            difficulty = DrillDifficulty.HARD,
            tier = TacticalTier.TIER_3,
            question = "[TIER-3 APT RED-TEAM DRILL] A cloud security architect is hardening a multi-tenant Kubernetes payment cluster against container breakout and hypervisor memory scraping by a nation-state adversary. In addition to rootless containers and seccomp-bpf syscall filtering, the architect requires hardware-enforced RAM encryption so the cloud host OS and hypervisor cannot read plaintext enclave memory in use. Which technology must be provisioned?",
            options = listOf(
                "Confidential Computing utilizing Trusted Execution Environments (TEE / AMD SEV-SNP / Intel TDX)",
                "Software-Defined Wide Area Networking (SD-WAN) with IPsec transport mode",
                "Container Image Signing via Docker Content Trust without runtime memory isolation",
                "Elastic Load Balancing with TLS termination at the ingress controller"
            ),
            correctIndex = 0,
            rationale = "[APT COUNTERMEASURE VERIFIED]: Confidential Computing protects data IN USE by isolating workloads inside hardware-backed Trusted Execution Environments (TEEs) with real-time memory encryption (such as AMD SEV-SNP or Intel TDX). Even if an APT compromises the host hypervisor or kernel, enclave memory remains cryptographically isolated.",
            commandReference = "dmesg | grep -iE 'SEV-SNP|TDX|Trusted Execution'"
        ),
        DrillQuestion(
            vectorId = "VEC-SY0-701-489H",
            domainCode = "4.0",
            domainTitle = "4.0 SECURITY OPERATIONS",
            severity = ThreatSeverity.CRITICAL,
            difficulty = DrillDifficulty.HARD,
            tier = TacticalTier.TIER_1,
            question = "A SIEM correlation rule flags an Active Directory service account requesting RC4-encrypted Kerberos TGS tickets (`0x17`) for multiple SPNs across the forest within 3 seconds, followed by offline hash cracking attempts. Which remediation step directly hardens the domain against this Kerberoasting vector?",
            options = listOf(
                "Enforce AES-256 Kerberos encryption (`msDS-SupportedEncryptionTypes`), disable RC4, and migrate service accounts to Group Managed Service Accounts (gMSA) with 120-character rotating passwords",
                "Increase the Kerberos TGT renewal lifetime from 7 days to 30 days on all Domain Controllers",
                "Configure NTLMv1 fallback authentication across member servers",
                "Add the service account SPNs to the Pre-Windows 2000 Compatible Access security group"
            ),
            correctIndex = 0,
            rationale = "[KERBEROASTING MITIGATED]: Kerberoasting abuses weak RC4-HMAC (`0x17`) encryption and static, human-chosen service account passwords to crack TGS tickets offline. Enforcing AES-256 encryption, disabling RC4, and adopting Group Managed Service Accounts (gMSA) with automatic 120-character password rotation renders offline cracking computationally infeasible.",
            commandReference = "Get-WinEvent -FilterHashtable @{LogName='Security';ID=4769} | Where-Object {\$_.Message -match '0x17'}"
        )
    )
}
