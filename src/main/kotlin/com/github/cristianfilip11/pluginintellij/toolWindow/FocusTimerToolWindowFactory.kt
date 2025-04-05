package com.github.cristianfilip11.pluginintellij.toolWindow


import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.io.File
import java.util.*
import java.util.Timer
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.swing.*
class FocusTimerToolWindowFactory : ToolWindowFactory {

   /* init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }*/

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val panel = JPanel(BorderLayout())
        private val timeLabel = JLabel("25:00", SwingConstants.CENTER)
        private val button = JButton("Start")
        private val focusTimeInput = JSpinner(SpinnerNumberModel(25, 1, 120, 1))  // Spinner for custom timer (1 to 120 minutes)
        private var timer: Timer? = null
        private var secondsLeft = 10

        init {
            timeLabel.font = timeLabel.font.deriveFont(32f)

            val inputPanel = JPanel()
            inputPanel.add(JLabel("Set Focus Time (minutes):"))
            inputPanel.add(focusTimeInput)

            panel.add(timeLabel, BorderLayout.CENTER)
            panel.add(button, BorderLayout.SOUTH)
            panel.add(inputPanel, BorderLayout.NORTH)  // Add input panel for custom timer
            button.addActionListener { toggleTimer() }
        }

        fun getContent(): JPanel = panel

        private fun toggleTimer() {
            if (timer == null) {
                val focusTimeMinutes = focusTimeInput.value as Int
                secondsLeft = focusTimeMinutes * 60  // Convert to seconds
                startTimer()
                button.text = "Stop"
            } else {
                stopTimer()
                button.text = "Start"
            }
        }

        private fun startTimer() {
            timer = Timer()
            timer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    SwingUtilities.invokeLater {
                        secondsLeft--
                        updateTimeLabel()
                        if (secondsLeft <= 0) {
                            stopTimer()
                            showNotification()
                        }
                    }
                }
            }, 0, 1000)
        }

        private fun stopTimer() {
            timer?.cancel()
            timer = null
            secondsLeft = 25 * 60
            updateTimeLabel()
        }

        private fun updateTimeLabel() {
            val minutes = secondsLeft / 60
            val seconds = secondsLeft % 60
            timeLabel.text = String.format("%02d:%02d", minutes, seconds)
        }

        private fun showNotification() {

            playSound()

            val notification = com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("Focus Timer Notification Group")
                .createNotification(
                    "Focus Session Complete",
                    "Take a short break!",
                    NotificationType.INFORMATION
                )

            Notifications.Bus.notify(notification)
        }

        private fun playSound() {
            try {
                // Load the sound file from the resources folder
                val soundFile = this::class.java.classLoader.getResource("sounds/toaster.wav")
                if (soundFile != null) {
                    val audioInputStream = AudioSystem.getAudioInputStream(soundFile)
                    val clip: Clip = AudioSystem.getClip()

                    // Open the clip and play it
                    clip.open(audioInputStream)
                    clip.start()

                } else {
                    println("Sound file not found!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("DOIASHDOASHFOIAHFOIASHFOIAHSFOIHASFSFASFASFASFSAFASIFHASIOFH")// If there's any issue loading or playing the sound, print the stack trace
            }
        }



    }
}
