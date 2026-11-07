package com.safex.app.core.remote

/**
 * Registry of known remote-access / screen-sharing applications.
 *
 * These apps have legitimate uses. The risk comes from context — not mere installation.
 * Do NOT classify all remote-support apps as malware.
 */
object RemoteAccessAppRegistry {

    enum class RiskLevel { MEDIUM, HIGH }

    data class RemoteApp(
        val packageName: String,
        val displayName: String,
        val riskLevel: RiskLevel,
        val notes: String = ""
    )

    val knownApps: List<RemoteApp> = listOf(
        RemoteApp("com.anydesk.anydeskandroid",          "AnyDesk",                 RiskLevel.HIGH,   "Full remote control"),
        RemoteApp("com.teamviewer.quicksupport.market",  "TeamViewer QuickSupport", RiskLevel.HIGH,   "Remote support"),
        RemoteApp("com.teamviewer.teamviewer",           "TeamViewer",              RiskLevel.HIGH,   "Full remote control"),
        RemoteApp("com.carriez.rustdesk",                "RustDesk",                RiskLevel.HIGH,   "Open-source remote desktop"),
        RemoteApp("com.splashtop.remote",                "Splashtop",               RiskLevel.MEDIUM, "Remote desktop"),
        RemoteApp("com.zoho.assist",                     "Zoho Assist",             RiskLevel.MEDIUM, "Remote support"),
        RemoteApp("net.remotix.android.rdp",             "Remotix",                 RiskLevel.MEDIUM, "RDP/VNC client"),
        RemoteApp("com.realvnc.viewer.android",          "VNC Viewer",              RiskLevel.HIGH,   "VNC client"),
        RemoteApp("com.xtralogic.android.rdpclient",     "Remote Desktop",          RiskLevel.MEDIUM, "RDP client")
    )

    val packageNames: Set<String> = knownApps.map { it.packageName }.toSet()

    fun isRemoteAccessApp(packageName: String): Boolean = packageNames.contains(packageName)

    fun getApp(packageName: String): RemoteApp? = knownApps.find { it.packageName == packageName }

    fun getDisplayName(packageName: String): String =
        getApp(packageName)?.displayName ?: packageName
}
