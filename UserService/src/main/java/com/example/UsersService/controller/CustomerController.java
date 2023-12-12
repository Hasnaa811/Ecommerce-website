package com.example.UsersService.controller;

import com.example.UsersService.Responses.CustomResponse;
import com.example.UsersService.model.AuthResponseDTO;
import com.example.UsersService.model.CustomerDto.CustomerDTO;
import com.example.UsersService.model.CustomerDto.CustomerRequestDTO;
import com.example.UsersService.model.UserEntity;
import com.example.UsersService.repository.UserRepository;
import com.example.UsersService.security.JWTGenerator;
import com.example.UsersService.service.CustomerService;
import com.example.UsersService.utils.email.EmailRequest;
import com.example.UsersService.utils.email.EmailService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.util.Optional;


///////////////////////////////////////////////////////
///////////////         Customers       ///////////////
///////////////////////////////////////////////////////

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/v1/customers")
@Data @NoArgsConstructor @AllArgsConstructor
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTGenerator jwtGenerator;

    private EmailService emailService;
    @Autowired
    public CustomerController(EmailService emailService) {
        this.emailService = emailService;
    }

    ////////////////////////////////////
///////////////  Login
////////////////////////////////////
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginCostumer(@RequestBody ObjectNode json){
        String userEmail = json.get("email").asText();
        String userPassword = json.get("password").asText();
        Optional<UserEntity> costumerData = customerService.getCostumerByEmail(userEmail);
        // Does UserEntity Exist in Db ?
        if(costumerData.isEmpty()){
            System.out.println("Customer Doesn't Exist");
            return ResponseEntity.badRequest().build();
            //   return
        }

        // Only Active UserEntity
        if(!costumerData.get().getActive()){
            System.out.println("Customer is not active");
            return ResponseEntity.badRequest().build();
        }
        // password
        String encodedPass = customerService.encodePassBcrypt(userPassword);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(userPassword, costumerData.get().getPassword())){
            // Passwords match, proceed with login
            System.out.println("Right Password");
            //CustomerProfileDTO customerProfileDTO = modelMapper.map(costumerData.get(), CustomerProfileDTO.class);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            costumerData.get().getUsername(),
                            userPassword));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            return new ResponseEntity<>(new AuthResponseDTO(token), HttpStatus.OK);
        } else {
            // Passwords do not match, handle login failure
            System.out.println("Wrong Password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }



///////////////////////////////////////
///////////////  Create a new customer
///////////////////////////////////////
/// When creating a new customer account, you can send a validation email to validate his account,
/// an extra verification process to check if the customer provided a correct email address
    @PostMapping(value= {"", "/"})
// TODO Only customers can create an account.
    public ResponseEntity<?> createNewCostumer(@RequestBody CustomerRequestDTO customerRequestDto){
        System.out.println("---------- >" + customerRequestDto);
        // TODO check if the customer provided a correct email address

        //--------- is Email unique ?
        if(customerRequestDto.getEmail()!=null && customerService.isEmailExist(customerRequestDto.getEmail())){
            return ResponseEntity.status(400)
                    .body(new CustomResponse<>(400, "Email Already Exist!"));
        }

        String encodedPass = customerService.encodePassBcrypt(customerRequestDto.getPassword());
        customerRequestDto.setPassword(encodedPass);

        UserEntity customer = modelMapper.map(customerRequestDto, UserEntity.class);
        customer.setRole("CUSTOMER");
        customerService.saveCustomer(customer);

        //--------- Email Link Confirmation

//        String confirmationLink = "http://localhost:8080/v1/customers/validate/"+customer.getId();
//        EmailRequest emailRequest = new EmailRequest(customer.getEmail(),"Hi"+customer.getLastName()+"Welcome To Our Website", "Click Here To Confirm "+ confirmationLink); // to,subject,body
//        Context context = new Context();
//        context.setVariable("message", emailRequest.getBody()); // to subject templateName context
//        emailService.sendEmailWithHtmlTemplate(emailRequest.getTo(), emailRequest.getSubject(), "email-template",context);

        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, "Successfully added!"));
    }


///////////////////////////////////////
///////////////  Customers list
///////////////////////////////////////
// Only users with admin and manager roles can list all the customers.
// If no customer exists you should return an empty array.
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("")
    // TODO  ADMIN and Manager
    public ResponseEntity<?> getCostumerList(@RequestParam(name = "page", defaultValue = "1") int page,
                                            @RequestParam(name = "order", defaultValue = "DESC") String order,
                                            @RequestParam(name = "size", defaultValue = "5") int size,
                                            @RequestParam(name = "sort", defaultValue = "id") String field){
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.fromString(order), field));
        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, userRepository.findAll(pageable)));
    }


///////////////////////////////////////
///////////////  Search For a Customer
///////////////////////////////////////
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/search")
    // TODO Only users with admin and manager
    public ResponseEntity<?> searchCostumer(@RequestParam(name = "page", defaultValue = "1") int page,
                                           @RequestParam(name = "sort", defaultValue = "DESC") String order,
                                           @RequestParam(name = "size", defaultValue = "10") int size,
                                           @RequestParam(name = "query") String query,
                                           @RequestParam(name = "field", defaultValue = "first_name") String field){

        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, customerService.searchCustomer(page, order, size, query, field)));
    }



///////////////////////////////////////
///////////////  Customer by ID
///////////////////////////////////////
// TODO Only users with admin and manager roles
// can get the customer's details.
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCostumerByID(@PathVariable("id") String customerID ){

        //--------- User Id Exist ?
        if(customerService.customerByIdExist(customerID)){
            return ResponseEntity.status(404)
                    .body(new CustomResponse<>(404, "Invalid Customer id"));
        }


        Optional<UserEntity> costumerFounded = customerService.getCostumerByID(customerID);

        // TODO Map to customer Dto

        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, costumerFounded));

    }



//////////////////////////////////////////////////////////
///////////////  Validate the customer's account or email
//////////////////////////////////////////////////////////
// Validate the customer's email if you have chosen to send a validation email when creating a new customer's account (Optional)
// The valid account status should be set to true.

    @PutMapping("/validate/{id}")
    public ResponseEntity<?> validateCostumer(@PathVariable("id") String customerID){


        //--------- User Id Exist ?
        if(customerService.customerByIdExist(customerID)){
            return ResponseEntity.status(404)
                    .body(new CustomResponse<>(404, "Invalid Customer id"));
        }
        UserEntity customer = customerService.getCostumerByID(customerID).get();
        customer.setActive(true);
        customerService.saveCustomer(customer);

        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, "Successfully Email validated!"));
    }



//////////////////////////////////////////////////////////
///////////////  Update the customer's data.//////////////
//////////////////////////////////////////////////////////
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCostumer(@RequestBody CustomerDTO customerDto, @PathVariable("id") String customerID ){
        // TODO Only users with admin and manager roles.

        //--------- User Id Exist ?
        if(customerService.customerByIdExist(customerID)){
            return ResponseEntity.status(404)
                    .body(new CustomResponse<>(404, "Invalid Customer id"));
        }

        //--------- is Email unique ?
        if(customerDto.getEmail()!=null && customerService.isEmailExist(customerDto.getEmail())){
            return ResponseEntity.status(400)
                    .body(new CustomResponse<>(400, "The Email Already Exist!"));
        }
        //--------- Update Code
        UserEntity customer = modelMapper.map(customerDto, UserEntity.class);
        customerService.saveCustomer(customer);

        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, "Successfully updated!"));
    }


//////////////////////////////////////////////////////
///////////  Delete the customer's account
//////////////////////////////////////////////////////
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    @DeleteMapping("")
    public ResponseEntity<?> deleteCostumer(@RequestBody UserEntity customer,
                                                 @RequestHeader("Authorization") String authorizationHeader){
        // TODO Only the customers can perform this action

        //--------- Error Authorization
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(403)
                    .body(new CustomResponse<>(403, "Authorization Error"));
        }

        String customerID = jwtGenerator.getIdFromJWT(authorizationHeader.substring(7));

        //--------- User Id Exist ?
        if(customerService.customerByIdExist(customerID)){
            return ResponseEntity.status(404)
                    .body(new CustomResponse<>(404, "Invalid Customer id"));
        }

        Optional<UserEntity> userToDelete = customerService.getCostumerByID(customerID);
        if(userToDelete.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        userToDelete.get().setEmail("###DELETED###"+userToDelete.get().getEmail()); /// Add Prefix to the deleted email users
        userToDelete.get().setActive(false);
        customerService.saveCustomer(userToDelete.get());
        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, "Customer deleted successfully"));
    }

//////////////////////////////////////////////////////////
///////////////  Customer Profile
//////////////////////////////////////////////////////////
// You can get the customer ID by decoding its access token.
   // @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping("/profile")
    // TODO Only Customer Can Access
    public ResponseEntity<?> getCostumerProfile(@RequestHeader("Authorization") String authorizationHeader){
        System.out.println("--------- authorizationHeader : " +authorizationHeader);

        //--------- Error Authorization
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(403)
                    .body(new CustomResponse<>(403, "Authorization Error"));
        }

        String customerID = jwtGenerator.getIdFromJWT(authorizationHeader.substring(7));

        //--------- User Id Exist ?
        if(customerService.customerByIdExist(customerID)){
            return ResponseEntity.status(404)
                    .body(new CustomResponse<>(404, "Invalid Customer id"));
        }

        Optional<UserEntity> costumerFounded = customerService.getCostumerByID(customerID);

        UserEntity user = costumerFounded.get();
        System.out.println("---- Return done user ----> " + user);
        // TODO Map User to Customer DTO
            // CustomerProfileDTO customerProfileDto = modelMapper.map(user, CustomerProfileDTO.class);
           // System.out.println("---- Return done Profile Dto----> " + customerProfileDto);
         return ResponseEntity.status(200)
                 .body(new CustomResponse<>(200, user));
        }


//////////////////////////////////////////////////////////////
///////////////  Update the customer data by providing its ID
//////////////////////////////////////////////////////////////
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    @PatchMapping("/profile/update")
    // Only customers can access this endpoint.
    // TODO The customer email should be unique.
    public ResponseEntity<?> updateUserProfile(@RequestBody ObjectNode customerData,
                                               @RequestHeader("Authorization") String authorizationHeader){

        //--------- Error Authorization
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(403)
                    .body(new CustomResponse<>(403, "Authorization Error"));
        }

            String customerID = jwtGenerator.getIdFromJWT(authorizationHeader.substring(7));
            String customerEmail = customerData.get("email").asText();
            String customerUsername = customerData.get("username").asText();

            //--------- User Id Exist ?
            if(customerService.customerByIdExist(customerID)){
                return ResponseEntity.status(404)
                        .body(new CustomResponse<>(404, "Invalid Customer id"));
            }

            //--------- is Email unique ?
            if(customerEmail!=null && customerService.isEmailExist(customerEmail)){
                return ResponseEntity.status(400)
                        .body(new CustomResponse<>(400, "The Email Already Exist"));
            }

            //--------- is Username Exist Already ?
            if(customerUsername!=null && customerService.isUsernameExist(customerUsername)){
                  return ResponseEntity.status(400)
                        .body(new CustomResponse<>(400, "the Username should be unique"));
            }

            //--------- Update Customer
            customerService.updateProfile(customerID, customerData);

        return ResponseEntity.status(200)
                .body(new CustomResponse<>(200, "Product updated successfully"));

    }
}