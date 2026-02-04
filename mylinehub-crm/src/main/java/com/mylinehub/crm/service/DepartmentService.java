package com.mylinehub.crm.service;


import com.mylinehub.crm.entity.Departments;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.DepartmentDTO;
import com.mylinehub.crm.exports.pdf.ExportDepartmentsToPDF;
import com.mylinehub.crm.exports.excel.BulkUploadDepartmentsToDatabase;
import com.mylinehub.crm.exports.excel.ExportDepartmentsToXLSX;
import com.mylinehub.crm.mapper.DepartmentMapper;
import com.mylinehub.crm.repository.DepartmentsRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class DepartmentService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
	
	private final EmployeeRepository employeeRepository;
    private final DepartmentsRepository departmentsRepository;
    private final DepartmentMapper departmentMapper;
    private final ErrorRepository errorRepository;
    
    public DepartmentDTO getByDepartmentNameAndOrganization(String departmentName,String organization) {
    	return departmentMapper.mapDepartmentToDto(departmentsRepository.getDepartmentByDepartmentNameAndOrganization(departmentName, organization));
    }
    
    public DepartmentDTO getDepartmentByIdAndOrganization(Long id,String organization) {
    	return departmentMapper.mapDepartmentToDto(departmentsRepository.getDepartmentByIdAndOrganization(id, organization));
    }
    
    public Departments getDepartmentByDepartmentNameAndOrganization(String departmentName,String organization) {
    	return departmentsRepository.getDepartmentByDepartmentNameAndOrganization(departmentName, organization);
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<DepartmentDTO> getAllDepartmentsOnOrganization(String organization){
        return departmentsRepository.findAllByOrganization(organization)
                .stream()
                .map(departmentMapper::mapDepartmentToDto)
                .collect(Collectors.toList());
    }
    
   
  
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean createDepartmentByOrganization(DepartmentDTO departmentDetails) {
    	
    	Departments current = departmentsRepository.getDepartmentByDepartmentNameAndOrganization(departmentDetails.getDepartmentName(),departmentDetails.getOrganization());
    	
    	if(current==null)
    	{
    		current = departmentMapper.mapDTOToDepartment(departmentDetails);
    		departmentsRepository.save(current);
    	}
    	else
    	{
    		
    		
    		return false;
    	}
    	
        return true;
    }

    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateDepartmentByOrganization(DepartmentDTO departmentDetails) {
    	
    	Departments current = departmentsRepository.getDepartmentByIdAndOrganization(departmentDetails.getDepartmentId(),departmentDetails.getOrganization());
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{
    		
    		try
    		{

    			current.setCity(departmentDetails.getCity());
    			current.setDepartmentName(departmentDetails.getDepartmentName());
    			Optional<Employee> manager = employeeRepository.findById(departmentDetails.getManagerId());
    			current.setManagers(manager.get());
    			current.setOrganization(departmentDetails.getOrganization());
    			departmentsRepository.save(current);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			System.out.println("Exception while updating employee");
    			return false;
    		}
    		
    	}
    	
        return true;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean deleteDepartmentByIdAndOrganization(Long id, String organization) {
    	
    	Departments current = departmentsRepository.getDepartmentByIdAndOrganization(id,organization);
    	
    	if(current==null)
    	{
    		return false;
    	}
    	else
    	{    		
    		departmentsRepository.delete(current);
    	}
    	
        return true;
    }
    

    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcel(HttpServletResponse response) throws IOException{
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=departments_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Departments> departmentsList = departmentsRepository.findAll();

        ExportDepartmentsToXLSX exporter = new ExportDepartmentsToXLSX(departmentsList);
        exporter.export(response);
    }


    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcelOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=AMIConnection_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Departments> departmentsList = departmentsRepository.findAllByOrganization(organization);

        ExportDepartmentsToXLSX exporter = new ExportDepartmentsToXLSX(departmentsList);
        exporter.export(response);
    }
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=departments_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Departments> departmentsList = departmentsRepository.findAll();

        ExportDepartmentsToPDF exporter = new ExportDepartmentsToPDF(departmentsList);
        exporter.export(response);
    }
    
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDFOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=departments_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Departments> departmentsList = departmentsRepository.findAllByOrganization(organization);

        ExportDepartmentsToPDF exporter = new ExportDepartmentsToPDF(departmentsList);
        exporter.export(response);
    }

    /**
     * The method is to retrieve departments whose have the name specified by the user.
     * After downloading all the data about the department,
     * the data is mapped to dto which will display only those needed
     * @param name name of the department
     * @return details of specific departments
     */
    public List<DepartmentDTO> getAllDepartmentsByName(String name, Pageable pageable) {
        return departmentsRepository.getDepartmentsByDepartmentNameContainingIgnoreCase(name, pageable)
                .stream()
                .map(departmentMapper::mapDepartmentToDto)
                .collect(Collectors.toList());
    }
    
    
    public void uploadDepartmentsUsingExcel(MultipartFile file,String organization) throws Exception {
        try {
        	
        	//System.out.println("I am inside try of uoload Employee Service");
          List<Departments> departments = new BulkUploadDepartmentsToDatabase().excelToDepartments(this,file.getInputStream(),organization,errorRepository);
          departmentsRepository.saveAll(departments);
        } catch (IOException e) {
        	//System.out.println("I am inside catch of Upload service");
          throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
      }
    
    
    /**
     * The method is to retrieve all departments from the database and display them.
     *
     * After downloading all the data about the department,
     * the data is mapped to dto which will display only those needed
     * @return list of all departments with specification of data in DepartmentDTO
     */
    
    public List<DepartmentDTO> getAllDepartments(Pageable pageable){
        return departmentsRepository.findAllBy(pageable)
                .stream()
                .map(departmentMapper::mapDepartmentToDto)
                .collect(Collectors.toList());
    }

    /**
     * The method is to download a specific department from the database and display it.
     * After downloading all the data about the department,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the department to be searched for
     * @throws ResponseStatusException if the id of the department you are looking for does not exist throws 404 status
     * @return detailed data about a specific department
     */
    
    public DepartmentDTO getDepartmentByIdToDTO(Long id) {
        Departments departments = departmentsRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department cannot be found, the specified id does not exist"));
        return departmentMapper.mapDepartmentToDto(departments);
    }
    
    
    /**
     * The method is to download a specific department from the database and display it.
     * After downloading all the data about the department,
     * the data is mapped to dto which will display only those needed
     *
     * @param id id of the department to be searched for
     * @throws ResponseStatusException if the id of the department you are looking for does not exist throws 404 status
     * @return detailed data about a specific department
     */
    
    public Departments getDepartmentById(Long id) {
        Departments departments = departmentsRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department cannot be found, the specified id does not exist"));
        return departments;
    }
    

    /**
     * The task of the method is to add a department to the database.
     * @param department requestbody of the department to be saved
     * @return saving the department to the database
     */
    public Departments addNewDepartment(Departments department) {
        return departmentsRepository.save(department);
    }

    /**
     * Method deletes the selected department by id
     * @param id id of the department to be deleted
     * @throws ResponseStatusException if id of the department is incorrect throws 404 status with message
     */
    public void deleteDepartmentById(Long id) {
        try{
            departmentsRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The specified id does not exist");
        }
    }
    
}
