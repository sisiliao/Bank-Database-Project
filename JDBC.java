import java.sql.* ;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;


class JDBC
{
    public static void main ( String [ ] args ) throws SQLException {
        int command;
        int sqlCode = 0;      // Variable to hold SQLCODE
        String sqlState = "00000";  // Variable to hold SQLSTATE
        // connect to JDBC

        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
        } catch (Exception e) {
            System.out.println("Class not found");
        }

        String url = "jdbc:postgresql://comp421.cs.mcgill.ca:5432/cs421";
        String usernamestring = "twu223";
        String passwordstring = "crt3e#lA";
        Connection con = DriverManager.getConnection(url, usernamestring, passwordstring);

        if (con != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }

        //The big while loop
        /* 1. Query how much a client has already paid for one of his loans, how much more he needs to pay
           2. For any account and a specific date, show all of its transaction records.
           3. Insert a new employee
           4. CheckGovID
           5. BranchInfo
           */
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input your command (0-4)");
        command = sc.nextInt();
        while (command < 5) {
            try {
                if (command == 0) {
                    Query_LoanInfo(con);
                }
                if (command == 1) {
                    Query_ShowTrans(con);
                }
                if (command == 2) {
                    Query_InsertEmployee(con);
                }
                if (command == 3) {
                    Query_CheckGovID(con);
                }
                if (command == 4) {
                    Query_BranchInfo(con);
                }
            } catch (SQLException e) {
                sqlCode = e.getErrorCode(); // Get SQLCODE
                sqlState = e.getSQLState(); // Get SQLSTATE

                // Your code to handle errors comes here;
                // something more meaningful than a print would be good
                System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            } catch (Exception e2) {
                System.out.println("Invalid Input. Try again.");
            }finally{
                System.out.println("Please input your command (0-4)");
                sc.nextLine();
                command = sc.nextInt();
            }

        }
    }
    public static void Query_LoanInfo(Connection con) throws SQLException, Exception{
        PreparedStatement prepareCid = con.prepareStatement("SELECT SUM(a.amount) AS already_paid, SUM(b.amount) AS loan_total FROM (SELECT * FROM Payments WHERE loanid=?) AS a, (SELECT * FROM Loans WHERE loanid=?) as b");
        Scanner sc = new Scanner(System.in);

        System.out.println("Please input the loan id");
        int id = sc.nextInt();
        prepareCid.setInt(1, id);
        prepareCid.setInt(2, id);
        ResultSet rs = prepareCid.executeQuery();
        while (rs.next())
            System.out.println("already paid: " + rs.getInt(1) + "  total amount: " + rs.getInt(2));
    }

    public static void Query_BranchInfo(Connection con) throws SQLException{
        PreparedStatement prepareCid = con.prepareStatement("SELECT name, gender, department, tel_no FROM Employees WHERE branchID = ? AND department = ?");
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the branch id");
        int id = sc.nextInt();

        prepareCid.setInt(1, id);
        System.out.println("Please specify the department");
        sc.nextLine();
        String dept = sc.nextLine();

        prepareCid.setString(2, dept);
        ResultSet rs = prepareCid.executeQuery();
        while (rs.next()){
            System.out.println("Name:" + rs.getString(1)
                    + "  Gender:" + rs.getString(2)
                    + "  Department:" + rs.getString(3)
                    + "  tel.no:" + rs.getString(4)
            );
        }

    }

    public static void Query_InsertEmployee(Connection con) throws SQLException{
        PreparedStatement prepareCid = con.prepareStatement("INSERT INTO Employees Values(?, ?, ?, ?, ?, ?, ?)");

        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the new Employee id");
        int id = sc.nextInt();
        prepareCid.setInt(1,id);
        sc.nextLine();

        System.out.println("Please input the new Employee name:");
        String name = sc.nextLine();
        prepareCid.setString(2,name);

        System.out.println("Please input the new Employee gender(M/F)");
        String Gender = sc.nextLine();
        while ( !Gender.equals("M") && !Gender.equals("F") && !Gender.equals("N")){
            System.out.println("Invalid Input. Try again.");
            Gender = sc.nextLine();
        }
        prepareCid.setString(3,Gender);

        System.out.println("Please input the new Employee department:");
        String dept = sc.nextLine();
        prepareCid.setString(5,dept);

        System.out.println("Please input the new Employee DOB:");
        String DOB = sc.nextLine();
        Date BirthDate = DateParser(DOB);
        prepareCid.setDate(4,BirthDate);

        System.out.println("please input the tel_no of new Employee");
        String tel = sc.nextLine();
        prepareCid.setString(6,tel);

        System.out.println("please input the branch_id of the new Employee");
        int branch = sc.nextInt();
        sc.nextLine();
        prepareCid.setInt(7,branch);

        int rs = prepareCid.executeUpdate();
        System.out.println("UpdateResponse: " + rs);
        //INSERT INTO Employee Values(‘id’,’name’, ‘Gender’, ‘DOB’, ‘dept’);
        //INSERT INTO Worksfor Values(‘id1’, ‘id2’);

        PreparedStatement prepareCid2 = con.prepareStatement("INSERT INTO Worksfor Values(?, ?)");
        prepareCid2.setInt(1,id);
        System.out.println("Please input id of the boss of the new Employee(a minus number to indicate no boss)");
        int id2 = sc.nextInt();
        if (id2 > 0){
            prepareCid2.setInt(2, id2);
            int rs2 = prepareCid2.executeUpdate();
            System.out.println("UpdateResponse: " + rs);
        }
    }

    public static void Query_CheckGovID(Connection con) throws SQLException{
        PreparedStatement prepareCid = con.prepareStatement("SELECT Clients.clientid, Personal.sin FROM Clients INNER JOIN Personal ON Clients.clientid = Personal.clientid WHERE Clients.name = ?");
        PreparedStatement prepareCid2 = con.prepareStatement("SELECT Clients.clientid, Business.gov_reg_no FROM Clients INNER JOIN Business ON Clients.clientid = Business.clientid WHERE Clients.name = ?");
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the client name");
        String name = sc.nextLine();
        prepareCid.setString(1, name);
        prepareCid2.setString(1, name);

        ResultSet rs = prepareCid.executeQuery();
        while (rs.next())
            System.out.println("clientid: " + rs.getInt(1) + "  SIN:" + rs.getString(2));
        ResultSet rs2 = prepareCid2.executeQuery();
        while (rs2.next())
            System.out.println("clientid: " + rs2.getInt(1) + "  Gov_Reg_NO::" + rs2.getInt(2));
    }

    public static void Query_ShowTrans(Connection con)throws SQLException{
        PreparedStatement prepareCid = con.prepareStatement("SELECT acc_no, amount, date, type FROM Transactions WHERE acc_no = ? AND date >= ?");
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the account_no");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.println("Please input the date");
        String date_s = sc.nextLine();
        Date date = DateParser(date_s);
        System.out.println(date.toString());
        prepareCid.setInt(1, id);
        prepareCid.setDate(2, date);
        ResultSet rs = prepareCid.executeQuery();
        while (rs.next())
            System.out.println("Date: " + rs.getDate("date") + "  Amount: " + rs.getInt("amount") + "  Type: " + rs.getString("type"));
    }

    public static Date DateParser(String date){
        int date_i = Integer.parseInt(date);
        int year = date_i / 10000;
        int month = (date_i - year * 10000) / 100;
        int day = (date_i - year * 10000 - month * 100);
        return new Date(year - 1900, month - 1, day);
    }
}





