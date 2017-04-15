import groovy.io.FileType
import groovy.swing.SwingBuilder
import groovy.transform.Field
import org.apache.tools.ant.util.FileUtils

import javax.swing.JList
import java.nio.file.Files

import static java.awt.GridBagConstraints.EAST
import static java.awt.GridBagConstraints.HORIZONTAL
import static java.awt.GridBagConstraints.NONE
import static java.awt.GridBagConstraints.REMAINDER
import static java.awt.GridBagConstraints.WEST
import static javax.swing.JFrame.EXIT_ON_CLOSE

@Field File userHomeDir = new File(System.getProperty('user.home'))
@Field File minecraftBaseDir = new File(userHomeDir, 'Library/Application Support/minecraft')
@Field File minecraftSavesDir = new File(minecraftBaseDir, 'saves')
@Field File backupBaseDir = new File(userHomeDir, 'Dropbox/minecraft-backups')
@Field File backupSavesDir = new File(backupBaseDir, 'saves')

new SwingBuilder().edt {
    frame(
            title: 'Minecraft Backup',
            minimumSize: [300, 120],
            pack: true,
            show: true,
            locationRelativeTo: null,
            defaultCloseOperation: EXIT_ON_CLOSE) {
        lookAndFeel 'nimbus'
        gridBagLayout()
        label(
                constraints: gbc(
                        gridx: 0,
                        gridy: 0,
                        gridwidth: 1,
                        gridheight: 1,
                        fill: NONE,
                        anchor: WEST,
                        insets: [5, 5, 0, 0],
                        weightx: 0,
                        weighty: 0
                ),
                text: 'Saves Directory:'
        )
        textField(
                constraints: gbc(
                        gridx: 1,
                        gridy: 0,
                        gridwidth: REMAINDER,
                        gridheight: 1,
                        fill: HORIZONTAL,
                        anchor: EAST,
                        insets: [5, 5, 0, 0],
                        weightx: 0,
                        weighty: 0
                ),
                text: minecraftSavesDir.getCanonicalPath()
        )
        JList directoryNamesJList = list(
                constraints: gbc(
                        gridx: 0,
                        gridy: 1,
                        gridwidth: REMAINDER,
                        gridheight: 1,
                        fill: HORIZONTAL,
                        anchor: EAST,
                        insets: [5, 5, 0, 0],
                        weightx: 0,
                        weighty: 0
                ),
                listData: getSavesDirectoryNames()
        )
        button(
                constraints: gbc(
                        gridx: 0,
                        gridy: 2,
                        gridwidth: REMAINDER,
                        gridheight: 1,
                        fill: HORIZONTAL,
                        anchor: EAST,
                        insets: [5, 5, 0, 0],
                        weightx: 0,
                        weighty: 0
                ),
                text: 'Backup',
                actionPerformed: {
                    backup(directoryNamesJList.getSelectedValuesList())
                }
        )
    }
}

List<String> getSavesDirectoryNames() {
    List<String> savesDirectorNames = []
    minecraftSavesDir.eachFile(FileType.DIRECTORIES) {
        savesDirectorNames << it.name
    }
    return savesDirectorNames
}

def backup(List<String> directoryNamesToBackup) {
    if (!backupSavesDir.exists() && !backupSavesDir.mkdirs()) {
        throw new RuntimeException("Unable to create directory $backupSavesDir")
    }

    directoryNamesToBackup.each { String sourceDirectoryToBackupName ->
        File sourceDirectoryToBackup = new File(minecraftSavesDir, sourceDirectoryToBackupName)
        File targetDirectoryToBackupTo = new File(
                backupSavesDir,
                sourceDirectoryToBackupName + '-' + new Date().format('yyyy-MM-dd-HHmmss')
        )

        println "Copy from $sourceDirectoryToBackup to $targetDirectoryToBackupTo"
        // TODO Copy the directory
        // See http://tutorials.jenkov.com/java-nio/files.html#overwriting-existing-files
    }
}