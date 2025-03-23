/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mo.it101.cltraltelite;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * @author Creative 1
 */
public class MOIT101CLTRALTELITE {
    private static final String FILE_NAME = "EmployeeDetailsUTF.csv";
    private static final String ATTENDANCE = "AttendanceRecord.csv";

    // Add employee
    public static void addEmployee(String id, String name, double salary) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(id + "," + name + "," + salary);
            writer.newLine();
            System.out.println("Employee record added successfully.");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // View single employee
    public static void viewEmployees() {
        Scanner inp = new Scanner(System.in);

        System.out.print("Enter Employee No.: ");
        String empNum = inp.nextLine(); // Read input as String to match CSV format

        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;

            System.out.println("\nSearching for Employee No. " + empNum + "...\n");

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].equals(empNum)) {
                    System.out.println("Employee Found: " + line);
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("Employee not found.");
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // View all employees
    public static void viewAllEmployeeRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            System.out.println("\nEmployee Records:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    

    // Read Attendance Records for Specific Date and Compute Salary
    public static double computeSalaryOnSpecificDate(String empID, String specificDate) {
        double hourlyRate = 0.0;
        double totalWorkHoursOnSpecificDate = 0.0; // Total hours worked

        // Read employee file to get hourly rate
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 19 && data[0].trim().equals(empID)) {
                    try {
                        // Ensure the last column is properly cleaned
                        String hourlyRateString = data[data.length - 1].trim();
                        hourlyRateString = hourlyRateString.replaceAll("[\",]", ""); // Remove commas and quotes

                        hourlyRate = Double.parseDouble(hourlyRateString);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing hourly rate for Employee ID " + empID + ": " + data[data.length - 1]);
                        return 0.0;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
            return 0.0;
        }
        

        // Read attendance records and calculate total work hours
        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE))) {
            String line;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 6) {
                    continue;
                }

                String recordEmpID = data[0].trim();
                String date = data[3].trim();
                String logInTime = data[4].trim();
                String logOutTime = data[5].trim();

                if (!recordEmpID.equals(empID)) {
                    continue;
                }
                if (!date.equals(specificDate)) {
                    continue; // Match specific date
                }
                try {
                    Date logIn = timeFormat.parse(logInTime);
                    Date logOut = timeFormat.parse(logOutTime);

                    // Compute hours worked correctly
                    long diffMillis = logOut.getTime() - logIn.getTime();
                    double hoursWorked = (diffMillis / (1000.0 * 60 * 60)); // Convert milliseconds to hours
                    hoursWorked = Math.round(hoursWorked * 100.0) / 100.0; // Round to 2 decimal places

                    totalWorkHoursOnSpecificDate += hoursWorked;
                } catch (Exception e) {
                    System.out.println("Skipping invalid time entry: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }

        // Compute Salary for the specific date
        double salaryForSpecificDate = totalWorkHoursOnSpecificDate * hourlyRate;

        // Debugging Output
        System.out.println("\n=== Salary Computation Debugging ===");
        System.out.println("Employee ID: " + empID);
        System.out.println("Date: " + specificDate);
        System.out.println("Total Work Hours: " + totalWorkHoursOnSpecificDate);
        System.out.println("Hourly Rate: PHP " + hourlyRate);
        System.out.println("Salary for " + specificDate + ": PHP " + salaryForSpecificDate);
        System.out.println("===================================");

        return salaryForSpecificDate;
    }

    // Compute Salary for the specific week
    public static double computeSalaryForWeek(String empID, String startDate, String endDate) {
        double hourlyRate = 0.0;
        double totalWorkHoursForWeek = 0.0;

        // Read employee file to get hourly rate
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 19 && data[0].trim().equals(empID)) {
                    try {
                        String hourlyRateString = data[data.length - 1].trim().replaceAll("[\",]", "");
                        hourlyRate = Double.parseDouble(hourlyRateString);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing hourly rate for Employee ID " + empID);
                        return 0.0;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
            return 0.0;
        }

        // Read attendance records to calculate total hours in the given week
        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE))) {
            String line;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            reader.readLine(); // Skip header

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 6) {
                    continue;
                }

                String recordEmpID = data[0].trim();
                String recordDate = data[3].trim();
                String logInTime = data[4].trim();
                String logOutTime = data[5].trim();

                if (!recordEmpID.equals(empID)) {
                    continue;
                }

                LocalDate currentDate = LocalDate.parse(recordDate);
                if (currentDate.isBefore(start) || currentDate.isAfter(end)) {
                    continue;
                }

                try {
                    Date logIn = timeFormat.parse(logInTime);
                    Date logOut = timeFormat.parse(logOutTime);

                    long diffMillis = logOut.getTime() - logIn.getTime();
                    double hoursWorked = (diffMillis / (1000.0 * 60 * 60));
                    totalWorkHoursForWeek += Math.round(hoursWorked * 100.0) / 100.0;
                } catch (Exception e) {
                    System.out.println("Skipping invalid time entry: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }

        // Compute weekly salary
        double salaryForWeek = totalWorkHoursForWeek * hourlyRate;
        return salaryForWeek;
    }

    // Delete employee
    public static void deleteEmployee(String idToDelete) {
        File inputFile = new File(FILE_NAME);
        File tempFile = new File("temp.csv");

        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile)); BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].equals(idToDelete)) {
                    found = true;
                    continue; // Skip writing this line to delete the employee
                }
                writer.write(line);
                writer.newLine();
            }

            if (found) {
                System.out.println("Employee with ID " + idToDelete + " deleted successfully.");
            } else {
                System.out.println("Employee ID not found.");
            }

        } catch (IOException e) {
            System.out.println("Error processing file: " + e.getMessage());
        }

        // Replace original file with updated file
        if (found) {
            if (!inputFile.delete()) {
                System.out.println("Could not delete the original file.");
                return;
            }
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("Could not rename the temporary file.");
            }
        }
    }

//    =======================================================================
    
    
    public static double computeMonthlySalary(String empID, String monthYear) {
        double totalWorkHours = 0.0;
        double hourlyRate = getHourlyRate(empID);


        if (hourlyRate == 0.0) {
            System.out.println("⚠ Employee not found or invalid hourly rate.");
            return 0.0;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE))) {
            String line;
            reader.readLine(); // Skip header
            
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 6) continue;

                String recordEmpID = data[0].trim();
                String recordDate = data[3].trim();
                String logInTime = data[4].trim();
                String logOutTime = data[5].trim();

                if (!recordEmpID.equals(empID) || !recordDate.startsWith(monthYear)) continue;

                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    Date logIn = timeFormat.parse(logInTime);
                    Date logOut = timeFormat.parse(logOutTime);

                    long diffMillis = logOut.getTime() - logIn.getTime();
                    double hoursWorked = diffMillis / (1000.0 * 60 * 60); // Convert to hours
                    totalWorkHours += hoursWorked;
                } catch (Exception e) {
                    System.out.println("⚠ Skipping invalid time entry: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Error reading attendance file: " + e.getMessage());
        }

        double grossSalary = totalWorkHours * hourlyRate;
        return applyDeductions(empID, grossSalary);
    }

    public static double getHourlyRate(String empID) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 19 && data[0].trim().equals(empID)) {
                    try {
                        return Double.parseDouble(data[data.length - 1].trim().replaceAll("[\",]", ""));
                    } catch (NumberFormatException e) {
                        System.out.println("⚠ Invalid hourly rate for Employee ID " + empID);
                        return 0.0;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Error reading employee file: " + e.getMessage());
        }
        return 0.0;
    }

    public static double applyDeductions(String empID, double grossSalary) {
        String employeeName = "";
        String employeePosition = "";
        double sss = getSSSContribution(grossSalary);
        double philHealth = getPhilHealthContribution(grossSalary);
        double pagIbig = getPagIbigContribution(grossSalary);
        double taxableIncome = grossSalary - (sss + philHealth + pagIbig);
        double tax = getTaxAmount(taxableIncome);
        double totalDeductions = sss + philHealth + pagIbig + tax;
        double riceSubsidy = 1500.00, phoneAllowance = 2000.00, clothingAllowance = 1000.00;
        double benefitsTotal = riceSubsidy + phoneAllowance + clothingAllowance;
        double netSalary = taxableIncome - tax;
        
        System.out.printf("\n========== 🧾 PAYSLIP ==========\n");
        
        System.out.println("\n\t\tMotorPH");
        System.out.println("\t\t7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City");
        System.out.println("\t\tPhone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073");
        System.out.println("\t\tEmail: corporate@motorph.com\n");
        System.out.println("EMPLOYEE PAYSLIP\n");
        System.out.printf("EMPLOYEE NAME\t%s\tEMPLOYEE POSITION/DEPARTMENT\t%s\n\n", employeeName, employeePosition);

        
        
        System.out.printf("👤 Employee ID: %s%n", empID);
        System.out.printf("💰 Gross Salary: PHP %.2f%n", grossSalary);
        System.out.printf("📉 SSS Deduction: PHP %.2f%n", sss);
        System.out.printf("📉 PhilHealth Deduction: PHP %.2f%n", philHealth);
        System.out.printf("📉 Pag-IBIG Deduction: PHP %.2f%n", pagIbig);
        System.out.printf("📈 Taxable Income: PHP %.2f%n", taxableIncome);
        System.out.printf("💸 Withholding Tax: PHP %.2f%n", tax);
        System.out.printf("🤑 Net Salary: PHP %.2f%n", netSalary);
        System.out.printf("================================\n");


        System.out.println("BENEFITS");
        System.out.printf("Rice Subsidy\t₱%.2f\n", riceSubsidy);
        System.out.printf("Phone Allowance\t₱%.2f\n", phoneAllowance);
        System.out.printf("Clothing Allowance\t₱%.2f\n", clothingAllowance);
        System.out.printf("TOTAL\t₱%.2f\n\n", benefitsTotal);

        System.out.println("DEDUCTIONS");
        System.out.printf("Social Security System\t\t₱%.2f\n", sss);
        System.out.printf("Philhealth\t\t₱%.2f\n", philHealth);
        System.out.printf("Pag-Ibig\t\t₱%.2f\n", pagIbig);
        System.out.printf("Withholding Tax\t\t₱%.2f\n", tax);
        System.out.printf("TOTAL DEDUCTIONS\t\t₱%.2f\n\n", totalDeductions);

        System.out.println("SUMMARY");
        System.out.printf("Benefits\t\t₱%.2f\n", benefitsTotal);
        System.out.printf("Deductions\t\t₱%.2f\n", totalDeductions);
        System.out.printf("TAKE HOME PAY\t\t₱%.2f\n", netSalary);
        
        
        
        
        return netSalary;
    }

    public static double getSSSContribution(double salary) {
        if (salary <= 3250) return 135.00;
        else if (salary > 24750) return 1125.00;

        double[] salaryBrackets = {3750, 4250, 4750, 5250, 5750, 6250, 6750, 7250, 7750, 8250,
                                   8750, 9250, 9750, 10250, 10750, 11250, 11750, 12250, 12750, 13250,
                                   13750, 14250, 14750, 15250, 15750, 16250, 16750, 17250, 17750, 18250,
                                   18750, 19250, 19750, 20250, 20750, 21250, 21750, 22250, 22750, 23250,
                                   23750, 24250, 24750};
        double[] contributions = {157.50, 180, 202.50, 225, 247.50, 270, 292.50, 315, 337.50, 360,
                                  382.50, 405, 427.50, 450, 472.50, 495, 517.50, 540, 562.50, 585,
                                  607.50, 630, 652.50, 675, 697.50, 720, 742.50, 765, 787.50, 810,
                                  832.50, 855, 877.50, 900, 922.50, 945, 967.50, 990, 1012.50, 1035,
                                  1057.50, 1080, 1102.50};

        for (int i = 0; i < salaryBrackets.length; i++) {
            if (salary <= salaryBrackets[i]) return contributions[i];
        }
        return 1125.00;
    }

    public static double getPhilHealthContribution(double salary) {
        return Math.min(Math.max(salary * 0.03, 300), 1800) / 2;  // 50% is employee share
    }

    public static double getPagIbigContribution(double salary) {
        return Math.min(salary * 0.02, 100);  // Max contribution is PHP 100
    }

    public static double getTaxAmount(double taxableIncome) {
        if (taxableIncome <= 20832) return 0;
        else if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome <= 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome <= 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome <= 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        else return 200833.33 + (taxableIncome - 666667) * 0.35;
    }
// ==========================================================================

    // Main menu
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Add Employee");
            System.out.println("2. View Employee");
            System.out.println("3. View All Employee Records");
            System.out.println("4. Delete Employee");
            System.out.println("5. Compute Salary for a Specific Date");
            System.out.println("6. Compute Salary for Week (X) ");
            System.out.println("7. Payslip for the Month of (x)");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter Employee ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Enter Employee Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Hourly Rate: ");
                    double hourlyRate = scanner.nextDouble();
                    addEmployee(id, name, hourlyRate);
                    break;

                case 2:
                    System.out.print("Enter Employee No.: ");
                    String empNum = scanner.nextLine(); // Read input as String to match CSV format

                    boolean found = false;
                    String[] employeeData = null;

                    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
                        String line;
                        System.out.println("\nSearching for Employee No. " + empNum + "...\n");

                        while ((line = reader.readLine()) != null) {
                            String[] data = line.split(",");
                            if (data.length > 0 && data[0].equals(empNum)) {
                                employeeData = data;
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            System.out.println("Employee Found: " + String.join(", ", employeeData));

                            System.out.print("Enter the specific date (YYYY-MM-DD): ");
                            String specificDate = scanner.nextLine();
                            double salary = computeSalaryOnSpecificDate(empNum, specificDate);
                            System.out.printf("Salary on %s: PHP %.2f%n", specificDate, salary);

                        } else {
                            System.out.println("Employee not found.");
                        }

                    } catch (IOException e) {
                        System.out.println("Error reading file: " + e.getMessage());
                    }
                    break;

                case 3:
                    viewAllEmployeeRecords();
                    break;

                case 4:
                    System.out.print("Enter Employee ID to delete: ");
                    String idToDelete = scanner.nextLine();
                    deleteEmployee(idToDelete);
                    break;

                case 5:
                    System.out.print("Enter Employee No.: ");
                    String empNumForDate = scanner.nextLine();

                    System.out.print("Enter the specific date (YYYY-MM-DD): ");
                    String date = scanner.nextLine();

                    double salaryOnDate = computeSalaryOnSpecificDate(empNumForDate, date);
                    System.out.printf("Salary on %s: PHP %.2f%n", date, salaryOnDate);
                    break;

                case 6:
                    System.out.print("Enter Employee No.: ");
                    String empNumForWeek = scanner.nextLine();

                    System.out.print("Enter the starting date of the week (YYYY-MM-DD): ");
                    String weekStartDate = scanner.nextLine();

                    System.out.print("Enter the ending date of the week (YYYY-MM-DD): ");
                    String weekEndDate = scanner.nextLine();

                    double weeklySalary = computeSalaryForWeek(empNumForWeek, weekStartDate, weekEndDate);
                    System.out.printf("Salary for the week (%s to %s): PHP %.2f%n", weekStartDate, weekEndDate, weeklySalary);
                    System.out.println("The government contributions have NOT YET been deducted; they will be deducted from your total monthly salary.");
                    break;

                case 7:

                    System.out.print("Enter Employee No.: ");
                    String empID = scanner.nextLine();

                    System.out.print("Enter month (YYYY-MM): ");
                    String monthYear = scanner.nextLine();

                    double netSalary = computeMonthlySalary(empID, monthYear);
                    if (netSalary > 0) {
                        System.out.printf("\n✅ Payslip generated for Employee No. %s for %s%n", empID, monthYear);
                    }
                    break;

                case 8:
                    System.out.println("Exiting program.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
