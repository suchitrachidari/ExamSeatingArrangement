import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;

public class ExamSeatingArrangement extends Frame implements ActionListener {
    // Original fields (modified)
    TextField tfRows, tfColumns, tfTotalStudents;
    Checkbox cbYear1, cbYear2, cbYear3, cbYear4;
    Choice semesterChoice;
    Button btnGenerate, btnExportTXT, btnPrint, btnBack, btnLoadCSV, btnAddSubject;
    Panel mainPanel, inputPanel, outputPanel;
    CardLayout cardLayout;
    Label lblTitle;
    HashMap<String, Color> branchColors = new HashMap<>();
    ArrayList<Student> students = new ArrayList<>();
    ArrayList<Student> loadedStudents = new ArrayList<>();
    ArrayList<ArrayList<Student>> seatingArrangement = new ArrayList<>();
    
    // New fields for enhanced features
    ArrayList<ArrayList<ArrayList<Student>>> allRooms = new ArrayList<>();
    int currentRoomView = 0;
    Panel roomButtonPanel;
    ArrayList<Button> roomButtons = new ArrayList<>();
    
    // Subject-Year-Branch mapping
    Panel subjectPanel;
    ArrayList<SubjectMapping> subjectMappings = new ArrayList<>();
    TextArea subjectDisplayArea;

    Color bgColor = new Color(245, 247, 252);
    Color seatBg = new Color(240, 243, 249);
    Color seatBorder = new Color(200, 200, 200);

    public ExamSeatingArrangement() {
        setTitle("Smart Exam Seating System");
        setSize(1000, 720);
        setLayout(new BorderLayout());
        setBackground(bgColor);

        lblTitle = new Label("SMART EXAM SEATING SYSTEM", Label.CENTER);
        lblTitle.setFont(new Font("Verdana", Font.BOLD, 26));
        lblTitle.setBackground(new Color(25, 118, 211));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignment(Label.CENTER);
        lblTitle.setPreferredSize(new Dimension(1000, 50));
        add(lblTitle, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new Panel(cardLayout);

        setupInputPanel();
        setupOutputPanel();

        mainPanel.add(inputPanel, "INPUT");
        mainPanel.add(outputPanel, "OUTPUT");

        add(mainPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { dispose(); }
        });

        setVisible(true);
    }

    private void setupInputPanel() {
        inputPanel = new Panel(new GridBagLayout());
        inputPanel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        inputPanel.add(new Label("Total Students:"), gbc);
        tfTotalStudents = new TextField("50"); gbc.gridx = 1;
        inputPanel.add(tfTotalStudents, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        inputPanel.add(new Label("Total Rows in Room:"), gbc);
        tfRows = new TextField("5"); gbc.gridx = 1;
        inputPanel.add(tfRows, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        inputPanel.add(new Label("Columns per Row:"), gbc);
        tfColumns = new TextField("6"); gbc.gridx = 1;
        inputPanel.add(tfColumns, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        inputPanel.add(new Label("Select Semester:"), gbc);
        semesterChoice = new Choice();
        semesterChoice.add("1"); semesterChoice.add("2");
        gbc.gridx = 1;
        inputPanel.add(semesterChoice, gbc);
        row++;

        // Subject-Year-Branch mapping section
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        Label lblSubjects = new Label("Subject Mapping (Year-Branch-Subject):", Label.LEFT);
        lblSubjects.setFont(new Font("Arial", Font.BOLD, 13));
        inputPanel.add(lblSubjects, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        btnAddSubject = new Button("Add Subject Mapping");
        btnAddSubject.addActionListener(this);
        inputPanel.add(btnAddSubject, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        subjectDisplayArea = new TextArea("", 4, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
        subjectDisplayArea.setEditable(false);
        subjectDisplayArea.setBackground(Color.WHITE);
        inputPanel.add(subjectDisplayArea, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(bgColor);
        btnGenerate = new Button("Generate Arrangement"); btnGenerate.addActionListener(this); btnPanel.add(btnGenerate);
        btnLoadCSV = new Button("Load CSV"); btnLoadCSV.addActionListener(this); btnPanel.add(btnLoadCSV);
        inputPanel.add(btnPanel, gbc);
    }

    private void setupOutputPanel() { outputPanel = new Panel(new BorderLayout()); }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnGenerate) generateSeating();
        else if (e.getSource() == btnLoadCSV) loadFromCSV();
        else if (e.getSource() == btnExportTXT) exportToTXT();
        else if (e.getSource() == btnPrint) printArrangement();
        else if (e.getSource() == btnBack) cardLayout.show(mainPanel,"INPUT");
        else if (e.getSource() == btnAddSubject) showAddSubjectDialog();
        else {
            // Check if it's a room button
            for(int i = 0; i < roomButtons.size(); i++) {
                if(e.getSource() == roomButtons.get(i)) {
                    currentRoomView = i;
                    displayArrangement();
                    break;
                }
            }
        }
    }

    private void showAddSubjectDialog() {
        Dialog dialog = new Dialog(this, "Add Subject Mapping", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new Label("Year:"), gbc);
        Choice yearChoice = new Choice();
        yearChoice.add("1"); yearChoice.add("2"); yearChoice.add("3"); yearChoice.add("4");
        gbc.gridx = 1;
        dialog.add(yearChoice, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new Label("Branch:"), gbc);
        TextField tfBranch = new TextField(10);
        gbc.gridx = 1;
        dialog.add(tfBranch, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new Label("Subject:"), gbc);
        TextField tfSubject = new TextField(15);
        gbc.gridx = 1;
        dialog.add(tfSubject, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(bgColor);
        
        Button btnAdd = new Button("Add");
        btnAdd.addActionListener(ev -> {
            int year = Integer.parseInt(yearChoice.getSelectedItem());
            String branch = tfBranch.getText().trim().toUpperCase();
            String subject = tfSubject.getText().trim();
            
            if(branch.isEmpty() || subject.isEmpty()) {
                showMessage("Error", "Please fill all fields.");
                return;
            }
            
            SubjectMapping mapping = new SubjectMapping(year, branch, subject);
            subjectMappings.add(mapping);
            updateSubjectDisplay();
            
            // Update branch colors
            if(!branchColors.containsKey(branch)) {
                Color[] colors = {new Color(102,126,234), new Color(34,197,94), new Color(239,68,68), 
                                 new Color(234,179,8), new Color(139,92,246), new Color(6,182,212)};
                branchColors.put(branch, colors[branchColors.size() % colors.length]);
            }
            
            dialog.dispose();
        });
        btnPanel.add(btnAdd);
        
        Button btnCancel = new Button("Cancel");
        btnCancel.addActionListener(ev -> dialog.dispose());
        btnPanel.add(btnCancel);
        
        dialog.add(btnPanel, gbc);
        
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateSubjectDisplay() {
        StringBuilder sb = new StringBuilder();
        for(SubjectMapping sm : subjectMappings) {
            sb.append("Year ").append(sm.year).append(" - ")
              .append(sm.branch).append(" - ")
              .append(sm.subject).append("\n");
        }
        subjectDisplayArea.setText(sb.toString());
    }

    private void generateSeating() {
        try {
            seatingArrangement.clear(); 
            students.clear();
            allRooms.clear();
            currentRoomView = 0;
            
            if(subjectMappings.isEmpty() && loadedStudents.isEmpty()) {
                showMessage("Error", "Please add subject mappings or load CSV file.");
                return;
            }
            
            int totalStudents = Integer.parseInt(tfTotalStudents.getText().trim());
            int totalRows = Integer.parseInt(tfRows.getText().trim());
            int totalColumns = Integer.parseInt(tfColumns.getText().trim());
            int selectedSem = Integer.parseInt(semesterChoice.getSelectedItem());

            // Generate or load students
            if(!loadedStudents.isEmpty()) {
                students.addAll(loadedStudents);
            } else {
                // Generate students based on subject mappings
                int rollCounter = 1;
                int studentsPerMapping = totalStudents / subjectMappings.size();
                int remainder = totalStudents % subjectMappings.size();
                
                for(int i = 0; i < subjectMappings.size(); i++) {
                    SubjectMapping sm = subjectMappings.get(i);
                    int count = studentsPerMapping + (i < remainder ? 1 : 0);
                    
                    for(int j = 0; j < count; j++) {
                        String rollNo = generateRollNumber(sm.year, sm.branch, rollCounter++);
                        students.add(new Student(rollNo, sm.branch, sm.year, selectedSem, sm.subject));
                    }
                }
            }

            if(students.isEmpty()) {
                showMessage("Error", "No students generated.");
                return;
            }

            // Arrange students with constraint: no same (subject+year+branch) adjacent
            arrangeStudentsWithConstraints(totalRows, totalColumns);
            
            displayArrangement();
            cardLayout.show(mainPanel,"OUTPUT");
        } catch(Exception ex){ 
            showMessage("Error","Invalid input: " + ex.getMessage()); 
            ex.printStackTrace(); 
        }
    }

    private String generateRollNumber(int year, String branch, int counter) {
        String yearPrefix = "2" + (4 - year); // 24 for 1st year, 23 for 2nd year, etc.
        String branchCode = "A" + (branch.length() >= 2 ? branch.substring(0, 2) : branch);
        return yearPrefix + "071" + branchCode + String.format("%04d", counter);
    }

    private void arrangeStudentsWithConstraints(int totalRows, int totalColumns) {
        // Create unique groups based on Year+Branch+Subject combination
        HashMap<String, ArrayList<Student>> groups = new HashMap<>();
        
        for(Student s : students) {
            String key = s.year + "-" + s.branch + "-" + s.subject;
            groups.putIfAbsent(key, new ArrayList<>());
            groups.get(key).add(s);
        }

        // Shuffle each group to randomize within same group
        Random rand = new Random();
        for(ArrayList<Student> group : groups.values()) {
            Collections.shuffle(group, rand);
        }

        ArrayList<String> groupKeys = new ArrayList<>(groups.keySet());
        Collections.shuffle(groupKeys, rand);
        
        ArrayList<Student> arrangedStudents = new ArrayList<>();
        
        // Round-robin distribution to maximize spacing between same groups
        boolean hasMore = true;
        while(hasMore) {
            hasMore = false;
            for(String key : groupKeys) {
                ArrayList<Student> group = groups.get(key);
                if(group != null && !group.isEmpty()) {
                    arrangedStudents.add(group.remove(0));
                    hasMore = true;
                }
            }
        }

        // Apply additional constraint checking for adjacent seats
        ArrayList<Student> finalArrangement = new ArrayList<>();
        finalArrangement.add(arrangedStudents.get(0));
        
        for(int i = 1; i < arrangedStudents.size(); i++) {
            Student current = arrangedStudents.get(i);
            Student previous = finalArrangement.get(finalArrangement.size() - 1);
            
            // Check if same year+branch+subject
            if(current.year == previous.year && 
               current.branch.equals(previous.branch) && 
               current.subject.equals(previous.subject)) {
                // Try to find a different student to swap
                boolean swapped = false;
                for(int j = i + 1; j < arrangedStudents.size(); j++) {
                    Student candidate = arrangedStudents.get(j);
                    if(!(candidate.year == previous.year && 
                         candidate.branch.equals(previous.branch) && 
                         candidate.subject.equals(previous.subject))) {
                        // Swap
                        arrangedStudents.set(i, candidate);
                        arrangedStudents.set(j, current);
                        finalArrangement.add(candidate);
                        swapped = true;
                        break;
                    }
                }
                if(!swapped) {
                    finalArrangement.add(current); // No swap possible
                }
            } else {
                finalArrangement.add(current);
            }
        }

        int roomCapacity = totalRows * totalColumns;
        int totalSeats = finalArrangement.size();
        int numRooms = (int) Math.ceil((double) totalSeats / roomCapacity);

        // Distribute students across rooms with constraint checking
        int studentIndex = 0;
        for(int roomNum = 0; roomNum < numRooms; roomNum++) {
            ArrayList<ArrayList<Student>> room = new ArrayList<>();
            
            for(int r = 0; r < totalRows && studentIndex < finalArrangement.size(); r++) {
                ArrayList<Student> row = new ArrayList<>();
                
                for(int c = 0; c < totalColumns && studentIndex < finalArrangement.size(); c++) {
                    Student current = finalArrangement.get(studentIndex);
                    
                    // Check left neighbor (same row)
                    boolean validPlacement = true;
                    if(c > 0) {
                        Student left = row.get(c - 1);
                        if(current.year == left.year && 
                           current.branch.equals(left.branch) && 
                           current.subject.equals(left.subject)) {
                            validPlacement = false;
                        }
                    }
                    
                    // Check top neighbor (previous row, same column)
                    if(validPlacement && r > 0 && c < room.get(r - 1).size()) {
                        Student top = room.get(r - 1).get(c);
                        if(current.year == top.year && 
                           current.branch.equals(top.branch) && 
                           current.subject.equals(top.subject)) {
                            validPlacement = false;
                        }
                    }
                    
                    if(validPlacement) {
                        row.add(current);
                        studentIndex++;
                    } else {
                        // Try to find next valid student
                        boolean found = false;
                        for(int k = studentIndex + 1; k < finalArrangement.size() && k < studentIndex + 20; k++) {
                            Student candidate = finalArrangement.get(k);
                            boolean candidateValid = true;
                            
                            if(c > 0) {
                                Student left = row.get(c - 1);
                                if(candidate.year == left.year && 
                                   candidate.branch.equals(left.branch) && 
                                   candidate.subject.equals(left.subject)) {
                                    candidateValid = false;
                                }
                            }
                            
                            if(candidateValid && r > 0 && c < room.get(r - 1).size()) {
                                Student top = room.get(r - 1).get(c);
                                if(candidate.year == top.year && 
                                   candidate.branch.equals(top.branch) && 
                                   candidate.subject.equals(top.subject)) {
                                    candidateValid = false;
                                }
                            }
                            
                            if(candidateValid) {
                                row.add(candidate);
                                finalArrangement.set(k, current);
                                studentIndex++;
                                found = true;
                                break;
                            }
                        }
                        
                        if(!found) {
                            row.add(current);
                            studentIndex++;
                        }
                    }
                }
                room.add(row);
            }
            
            if(!room.isEmpty()) {
                allRooms.add(room);
            }
        }

        // Set first room as current seating arrangement
        if(!allRooms.isEmpty()) {
            seatingArrangement = allRooms.get(0);
        }
    }

    private void displayArrangement(){
        outputPanel.removeAll();
        Panel content = new Panel(new BorderLayout(10,10));
        
        // Room navigation buttons
        if(allRooms.size() > 1) {
            roomButtonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            roomButtonPanel.setBackground(new Color(230, 235, 245));
            roomButtons.clear();
            
            for(int i = 0; i < allRooms.size(); i++) {
                Button roomBtn = new Button("Room " + (i + 1));
                roomBtn.setFont(new Font("Arial", Font.BOLD, 12));
                if(i == currentRoomView) {
                    roomBtn.setBackground(new Color(25, 118, 211));
                    roomBtn.setForeground(Color.WHITE);
                } else {
                    roomBtn.setBackground(Color.WHITE);
                    roomBtn.setForeground(new Color(25, 118, 211));
                }
                roomBtn.addActionListener(this);
                roomButtons.add(roomBtn);
                roomButtonPanel.add(roomBtn);
            }
            content.add(roomButtonPanel, BorderLayout.NORTH);
        }

        // Display current room
        if(currentRoomView < allRooms.size()) {
            seatingArrangement = allRooms.get(currentRoomView);
        }

        Panel arrange = new Panel(new GridLayout(seatingArrangement.size(),1,10,10));
        arrange.setBackground(bgColor);

        for(ArrayList<Student> row: seatingArrangement){
            Panel rowPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            rowPanel.setBackground(bgColor);
            for(Student s: row) rowPanel.add(createSeatPanel(s));
            arrange.add(rowPanel);
        }

        ScrollPane sp = new ScrollPane();
        sp.add(arrange);
        content.add(sp, BorderLayout.CENTER);

        Panel legend = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        legend.setBackground(Color.WHITE);
        for(Map.Entry<String, Color> entry: branchColors.entrySet()){
            Panel box = new Panel();
            box.setBackground(entry.getValue());
            box.setPreferredSize(new Dimension(20,20));
            Label lbl = new Label(entry.getKey());
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            Panel item = new Panel(new FlowLayout(FlowLayout.LEFT,5,2));
            item.setBackground(Color.WHITE); item.add(box); item.add(lbl);
            legend.add(item);
        }
        content.add(legend, BorderLayout.SOUTH);

        Panel bottom = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setBackground(bgColor);
        btnExportTXT=new Button("Export TXT"); btnExportTXT.addActionListener(this); bottom.add(btnExportTXT);
        btnPrint=new Button("Print"); btnPrint.addActionListener(this); bottom.add(btnPrint);
        btnBack=new Button("Back"); btnBack.addActionListener(this); bottom.add(btnBack);

        outputPanel.add(content, BorderLayout.CENTER);
        outputPanel.add(bottom, BorderLayout.SOUTH);
        validate(); repaint();
    }

    private Panel createSeatPanel(Student s){
        Panel seat = new Panel(new GridLayout(4,1,0,2)){
            public void paint(Graphics g){
                g.setColor(seatBorder); g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                super.paint(g);
            }
        };
        seat.setBackground(Color.WHITE); seat.setPreferredSize(new Dimension(110,100));
        Label roll = new Label(s.rollNo,Label.CENTER); 
        roll.setFont(new Font("Verdana", Font.BOLD,11)); 
        roll.setForeground(new Color(25,118,211));
        
        Color bc = branchColors.getOrDefault(s.branch, new Color(102,126,234));
        Label branch = new Label(s.branch, Label.CENTER); 
        branch.setFont(new Font("Arial", Font.BOLD,10)); 
        branch.setBackground(bc); 
        branch.setForeground(Color.WHITE);
        
        Label year = new Label("Y"+s.year+" S"+s.semester, Label.CENTER); 
        year.setFont(new Font("Arial", Font.PLAIN,9)); 
        year.setForeground(Color.GRAY);
        
        Label subject = new Label(s.subject, Label.CENTER);
        subject.setFont(new Font("Arial", Font.ITALIC, 8));
        subject.setForeground(new Color(100, 100, 100));
        
        seat.add(roll); seat.add(branch); seat.add(year); seat.add(subject);
        return seat;
    }

    private void exportToTXT(){
        try(FileWriter writer=new FileWriter("seating_arrangement.txt")){
            for(int roomIdx = 0; roomIdx < allRooms.size(); roomIdx++) {
                writer.write("=== ROOM " + (roomIdx + 1) + " ===\n\n");
                ArrayList<ArrayList<Student>> room = allRooms.get(roomIdx);
                
                for(int i=0;i<room.size();i++){
                    writer.write("Row "+(i+1)+":\n");
                    for(Student s: room.get(i)) {
                        writer.write(s.rollNo+" - "+s.branch+" (Y"+s.year+" S"+s.semester+") - "+s.subject+"\n");
                    }
                    writer.write("\n");
                }
                writer.write("\n");
            }
            showMessage("Export Successful","Saved seating_arrangement.txt");
        }catch(IOException e){ showMessage("Error","Failed TXT export."); }
    }

    private void printArrangement(){
        PrinterJob job=PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            public int print(Graphics g, PageFormat pf, int pageIndex){
                if(pageIndex>0) return Printable.NO_SUCH_PAGE;
                g.translate((int)pf.getImageableX(), (int)pf.getImageableY());
                outputPanel.printAll(g);
                return Printable.PAGE_EXISTS;
            }
        });
        try{ if(job.printDialog()) job.print(); }catch(PrinterException ex){ showMessage("Print Error","Failed to print."); }
    }

    private void showMessage(String title,String message){
        Dialog d=new Dialog(this,title,true); d.setLayout(new BorderLayout());
        Label msg=new Label(message,Label.CENTER); msg.setFont(new Font("Arial",Font.PLAIN,14)); 
        d.add(msg,BorderLayout.CENTER);
        Button ok=new Button("OK"); ok.addActionListener(ev->d.dispose()); 
        d.add(ok,BorderLayout.SOUTH);
        d.setSize(400,150); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private void loadFromCSV(){
        FileDialog fd=new FileDialog(this,"Select CSV",FileDialog.LOAD); fd.setVisible(true);
        String dir=fd.getDirectory(); String file=fd.getFile(); if(dir==null||file==null) return;
        String path=dir+file; loadedStudents.clear();
        try(BufferedReader br=new BufferedReader(new FileReader(path))){
            String line; boolean firstRow=true;
            while((line=br.readLine())!=null){
                line=line.trim(); if(line.isEmpty()) continue;
                String[] parts=line.split(",", -1); if(parts.length<5) continue;
                if(firstRow){ 
                    String firstCell=parts[0].trim().toLowerCase(); 
                    if(firstCell.contains("roll")){ 
                        firstRow=false; continue;
                    } 
                    firstRow=false;
                }
                try{
                    String rollNo=parts[0].trim();
                    String branch=parts[1].trim().toUpperCase();
                    int year=Integer.parseInt(parts[2].trim());
                    int sem=Integer.parseInt(parts[3].trim());
                    String subject=parts[4].trim();
                    loadedStudents.add(new Student(rollNo,branch,year,sem,subject));
                    
                    // Update branch colors
                    if(!branchColors.containsKey(branch)) {
                        Color[] colors = {new Color(102,126,234), new Color(34,197,94), new Color(239,68,68), 
                                         new Color(234,179,8), new Color(139,92,246), new Color(6,182,212)};
                        branchColors.put(branch, colors[branchColors.size() % colors.length]);
                    }
                }catch(NumberFormatException nfe){}
            }
            showMessage("Load Complete","Loaded "+loadedStudents.size()+" records from CSV.");
        }catch(IOException ex){ showMessage("Error","Failed to load CSV."); }
    }

    class Student{
        String rollNo, branch, subject;
        int year, semester;
        
        Student(String rollNo, String branch, int year, int semester, String subject){
            this.rollNo=rollNo; 
            this.branch=branch; 
            this.year=year; 
            this.semester=semester;
            this.subject=subject;
        }
    }

    class SubjectMapping{
        int year;
        String branch, subject;
        
        SubjectMapping(int year, String branch, String subject){
            this.year = year;
            this.branch = branch;
            this.subject = subject;
        }
    }

    public static void main(String[] args){ new ExamSeatingArrangement(); }
}