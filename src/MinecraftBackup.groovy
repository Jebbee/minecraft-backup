import groovy.io.FileType
import groovy.swing.SwingBuilder
import groovy.transform.Field

import javax.swing.*

import static java.awt.GridBagConstraints.*
import static javax.swing.JFrame.EXIT_ON_CLOSE

@Field File userHomeDir = new File(System.getProperty('user.home'))
@Field File minecraftBaseDir = new File(userHomeDir, 'Library/Application Support/minecraft')
// TODO Why can we not reassign minecraftSavesDir when using "@Field File" declaration?
minecraftSavesDir = new File(minecraftBaseDir, 'saves')
@Field File backupBaseDir = new File(userHomeDir, 'Dropbox/minecraft-backups')
@Field File backupSavesDir = new File(backupBaseDir, 'saves')

@Field JFrame mainFrame
@Field JList minecraftSavesDirectoryNamesJList
@Field DefaultListModel<String> minecraftSavesDirectoryNamesListModel = new DefaultListModel<>()

populateMinecraftSavesDirectoryNamesListModel()

new SwingBuilder().edt {
    mainFrame = frame(
            title: 'Minecraft Backup',
            minimumSize: [300, 120],
            pack: true,
            show: true,
            locationRelativeTo: null,
            defaultCloseOperation: EXIT_ON_CLOSE) {
        lookAndFeel 'system'
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
        JTextField minecraftSavesDirTextField = textField(
                constraints: gbc(
                        gridx: 1,
                        gridy: 0,
                        gridwidth: 1,
                        gridheight: 1,
                        fill: HORIZONTAL,
                        anchor: CENTER,
                        insets: [5, 5, 0, 0],
                        weightx: 1,
                        weighty: 0
                ),
                text: minecraftSavesDir.getCanonicalPath()
        )
        button(
                constraints: gbc(
                        gridx: 2,
                        gridy: 0,
                        gridwidth: REMAINDER,
                        gridheight: 1,
                        fill: NONE,
                        anchor: CENTER,
                        insets: [5, 5, 0, 5],
                        weightx: 0,
                        weighty: 0
                ),
                text: 'Browse',
                actionPerformed: {
                    JFileChooser fileChooser = fileChooser(
                            dialogTitle: 'Choose a Minecraft world directory to backup',
                            fileSelectionMode: JFileChooser.DIRECTORIES_ONLY,
                            currentDirectory: minecraftSavesDir
                    )
                    int returnValue = fileChooser.showOpenDialog(mainFrame)

                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        minecraftSavesDir = fileChooser.getSelectedFile()
                        minecraftSavesDirTextField.text = minecraftSavesDir.getCanonicalPath()
                        populateMinecraftSavesDirectoryNamesListModel()
                    }
                }
        )
        scrollPane(
                constraints: gbc(
                        gridx: 0,
                        gridy: 1,
                        gridwidth: REMAINDER,
                        gridheight: 1,
                        fill: BOTH,
                        anchor: EAST,
                        insets: [5, 5, 0, 5],
                        weightx: 1,
                        weighty: 1
                )
        ) {
            minecraftSavesDirectoryNamesJList = list(
                    model: minecraftSavesDirectoryNamesListModel
            )
        }
        label(
                constraints: gbc(
                        gridx: 0,
                        gridy: 2,
                        gridwidth: 1,
                        gridheight: 1,
                        fill: NONE,
                        anchor: WEST,
                        insets: [5, 5, 0, 0],
                        weightx: 0,
                        weighty: 0
                ),
                text: 'Backup Directory:'
        )
        JTextField backupSavesDirTextField = textField(
                constraints: gbc(
                        gridx: 1,
                        gridy: 2,
                        gridwidth: 1,
                        gridheight: 1,
                        fill: HORIZONTAL,
                        anchor: WEST,
                        insets: [5, 5, 0, 0],
                        weightx: 1,
                        weighty: 0
                ),
                text: backupSavesDir.getCanonicalPath()
        )
        button(
                constraints: gbc(
                        gridx: 2,
                        gridy: 2,
                        gridwidth: REMAINDER,
                        gridheight: 1,
                        fill: NONE,
                        anchor: CENTER,
                        insets: [5, 5, 0, 5],
                        weightx: 0,
                        weighty: 0
                ),
                text: 'Browse',
                actionPerformed: {
                    JFileChooser fileChooser = fileChooser(
                            dialogTitle: 'Choose a directory to backup to',
                            fileSelectionMode: JFileChooser.DIRECTORIES_ONLY,
                            currentDirectory: backupSavesDir
                    )
                    int returnValue = fileChooser.showOpenDialog(mainFrame)

                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        backupSavesDir = fileChooser.getSelectedFile()
                        backupSavesDirTextField.text = backupSavesDir.getCanonicalPath()
                    }
                }
        )
        button(
                constraints: gbc(
                        gridx: 0,
                        gridy: 3,
                        gridwidth: REMAINDER,
                        gridheight: 1,
                        fill: NONE,
                        anchor: EAST,
                        insets: [5, 5, 5, 5],
                        weightx: 0,
                        weighty: 0
                ),
                text: 'Backup',
                actionPerformed: {
                    backup(minecraftSavesDirectoryNamesJList.getSelectedValuesList())
                }
        )
    }
}

def populateMinecraftSavesDirectoryNamesListModel() {
    minecraftSavesDirectoryNamesListModel.clear()
    println "minecraftSavesDir = $minecraftSavesDir"
    minecraftSavesDir.eachFile(FileType.DIRECTORIES) { directory ->
        minecraftSavesDirectoryNamesListModel.addElement(directory.name)
    }
}

def backup(List<String> directoryNamesToBackup) {
    if (!backupSavesDir.exists() && !backupSavesDir.mkdirs()) {
        throw new RuntimeException("Unable to create directory $backupSavesDir")
    }

    directoryNamesToBackup.each { String sourceDirectoryToBackupName ->
        File sourceDirectoryToBackup = new File(minecraftSavesDir as File, sourceDirectoryToBackupName)
        File targetDirectoryToBackupTo = new File(
                backupSavesDir,
                sourceDirectoryToBackupName + '-' + new Date().format('yyyy-MM-dd-HHmmss')
        )

        println "Copy from $sourceDirectoryToBackup to $targetDirectoryToBackupTo"
        // TODO Copy the directory
        // See http://tutorials.jenkov.com/java-nio/files.html#overwriting-existing-files
    }
}