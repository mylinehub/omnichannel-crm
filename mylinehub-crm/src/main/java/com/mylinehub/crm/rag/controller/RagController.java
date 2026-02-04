package com.mylinehub.crm.rag.controller;


import com.mylinehub.crm.rag.dto.IngestResponse;
import com.mylinehub.crm.rag.dto.ResultDTO;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.rag.dto.IngestRequest;
import com.mylinehub.crm.rag.service.EmbeddingService;
import com.mylinehub.crm.rag.service.IngestService;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import lombok.AllArgsConstructor;

import static com.mylinehub.crm.controller.ApiMapping.OPEN_API_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(produces="application/json", path = OPEN_API_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class RagController {

	private final EmbeddingService embeddingService;
    private final IngestService ingestService;
    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    
    
    @GetMapping("/vectorSearch")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<String>> vectorSearch(@RequestParam String organization,
                                                 @RequestParam String input,
                                                 @RequestHeader (name="Authorization") String token) {
    	List<String> r = null;
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	try
    	{
        	try {
        		r = this.embeddingService.searchSimilarEmbeddings(organization, input);
                return ResponseEntity.ok(r);
            } catch (Exception e) {
                System.out.println("[RagController] vectorSearch error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
            }
    	}
    	catch(Exception e) {
    		return status(HttpStatus.UNAUTHORIZED).body(r);
    	}
    }
    
    
    @PostMapping(value = "/ingest", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<IngestResponse> ingest(@RequestParam String organization,
                                                 @RequestParam String uploader,
                                                 @RequestParam MultipartFile file,@RequestHeader (name="Authorization") String token) {
    	IngestResponse r = null;
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
        	try {
                IngestResponse req = new IngestResponse();
                req.setOrganization(organization);
                req.setUploader(uploader);
                req.setFile(file);
                IngestResponse resp = ingestService.ingest(req);
                return ResponseEntity.ok(resp);
            } catch (Exception e) {
                System.out.println("[RagController] ingest error: " + e.getMessage());
                r = new IngestResponse();
                r.setSuccess(false);
                r.setMessage(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
            }
    	}
    	else {
    		return status(HttpStatus.UNAUTHORIZED).body(r);
    	}
    }
    
    @PostMapping("/ingest/url")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<IngestResponse> ingestUrl(
            @RequestParam String organization,
            @RequestParam String uploader,
            @RequestParam String url,@RequestHeader (name="Authorization") String token) {
        
    	IngestResponse r = null;
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
        	try {
        		IngestRequest req = new IngestRequest();
                req.setOrganization(organization);
                req.setUploader(uploader);
                req.setUrl(url);

                IngestResponse resp = ingestService.ingestUrl(req);
                return ResponseEntity.ok(resp);

            } catch (Exception e) {
                System.out.println("[RagController] ingestUrl error: " + e.getMessage());
                r = new IngestResponse();
                r.setSuccess(false);
                r.setMessage(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
            }
    	}
    	else {
    		return status(HttpStatus.UNAUTHORIZED).body(r);
    	}
    	
    }


    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<IngestResponse> deleteByHashes(@RequestParam String organization,
                                                         @RequestBody java.util.List<String> hashes,@RequestHeader (name="Authorization") String token) {
    	IngestResponse resp = null;
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    	   	
        	try {
                resp = ingestService.deleteByHashes(organization, hashes);
                return ResponseEntity.ok(resp);
            } catch (Exception e) {
                System.out.println("[RagController] delete error: " + e.getMessage());
                IngestResponse r = new IngestResponse();
                r.setSuccess(false);
                r.setMessage(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
            }
    	}
    	else {
    		return status(HttpStatus.UNAUTHORIZED).body(resp);
    	}
 
    }

}
