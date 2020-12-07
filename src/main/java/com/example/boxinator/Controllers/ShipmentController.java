package com.example.boxinator.Controllers;


import com.example.boxinator.Models.Account;
import com.example.boxinator.Models.Shipment;
import com.example.boxinator.Models.ShipmentDTO;
import com.example.boxinator.Models.Enums.ShipmentStatus;
import com.example.boxinator.Repositories.AccountRepository;
import com.example.boxinator.Repositories.ShipmentRepository;
import com.example.boxinator.Utils.AuthService.AuthResponse;
import com.example.boxinator.Utils.AuthService.AuthenticationService;
import com.example.boxinator.Utils.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/shipment")
public class ShipmentController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    AuthenticationService authService;

    // * POST/ (create new shipment)
    @PostMapping("/create")
    public ResponseEntity<CommonResponse> createShipment(@RequestHeader(value = "Authorization") String token, @RequestBody Shipment shipment) {
        CommonResponse cr = new CommonResponse();
        ResponseEntity<AuthResponse> authResponse = authService.checkToken(token);

        if (authService.checkToken(token).getStatusCode() == HttpStatus.OK) {
            try {
                shipmentRepository.save(shipment);
                cr.data = shipment;
                cr.msg = "Shipment created";
                cr.status = HttpStatus.CREATED;
            } catch (DataIntegrityViolationException e) {
                cr.msg = "Some required field might be missing.";
                cr.status = HttpStatus.BAD_REQUEST;
            } catch (Exception e) {
                cr.data = null;
                cr.msg = "Shipment could not be created";
                cr.status = HttpStatus.BAD_REQUEST;
            }
        } else {
            cr.data = authService.checkToken(token).getBody().msg;
            cr.msg = "Unauthorized: Invalid token.";
            cr.status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(cr, cr.status);
    }

    // * GET/:shipment_id (get details about specific shipment),
    @GetMapping("/{shipment_id}")
    public ResponseEntity<CommonResponse> getShipment(@PathVariable long shipment_id) {
        CommonResponse cr = new CommonResponse();

        if (shipmentRepository.existsById(shipment_id)) {
            Optional<Shipment> shipmentRepo = shipmentRepository.findById(shipment_id);
            Shipment shipment = shipmentRepo.orElse(null);

            cr.data = shipment;
            cr.msg = "Shipment found";
            cr.status = HttpStatus.OK;
        } else {
            cr.msg = "Shipment with id: " + shipment_id + " was not found.";
            cr.status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(cr, cr.status);
    }

    // * POST/:shipment_id (used to update a shipment, user can only cancel, admin can change status
    @PatchMapping("/{shipment_id}")
    public ResponseEntity<CommonResponse> updateShipment(
            @RequestBody Shipment newShipment,
            @PathVariable long shipment_id) {
        CommonResponse cr = new CommonResponse();

        if (shipmentRepository.existsById(shipment_id)) {
            Optional<Shipment> shipmentRepo = shipmentRepository.findById(shipment_id);
            Shipment shipment = shipmentRepo.orElse(null);


            if (newShipment.getWeight() != 0) {
                shipment.setWeight(newShipment.getWeight());
            }

            if (newShipment.getBoxColour() != null) {
                shipment.setBoxColour(newShipment.getBoxColour());
            }

            if (newShipment.getReceiver() != null) {
                shipment.setReceiver(newShipment.getReceiver());
            }

            if (newShipment.getDestinationCountry() != null) {
                shipment.setDestinationCountry(newShipment.getDestinationCountry());
            }

            if (newShipment.getSourceCountry() != null) {
                shipment.setSourceCountry(newShipment.getSourceCountry());
            }

            if (newShipment.getShipmentStatus() != null) {
                shipment.setShipmentStatus(newShipment.getShipmentStatus());
            }

            try {
                shipmentRepository.save(shipment);
                cr.data = shipment;
                cr.msg = "Shipment details has been updated.";
                cr.status = HttpStatus.CREATED;

            } catch (Exception e) {
                cr.status = HttpStatus.BAD_REQUEST;
            }

        } else {
            cr.msg = "Shipment with id: " + shipment_id + " was not found.";
        }

        return new ResponseEntity<>(cr, cr.status);
    }

    // *  DELETE/:shipment_id Only accessible by admin, only in extreme situations, can delete complete/cancelled shipments
    @DeleteMapping("/{shipment_id}")
    public ResponseEntity<CommonResponse> deleteShipment(@PathVariable long shipment_id) {
        CommonResponse cr = new CommonResponse();

        Optional<Shipment> shipmentRepo = shipmentRepository.findById(shipment_id);
        Shipment shipment = shipmentRepo.orElse(null);

        try {
            cr.data = shipment;
            shipmentRepository.deleteById(shipment_id);
            cr.msg = "Shipment deleted";
            cr.status = HttpStatus.CREATED;
        } catch (Exception e) {

        }
        return new ResponseEntity<>(cr, cr.status);
    }

    //     * GET/ (get all relevant to user, admin sees all, non-cancelled, non-complete, can be filtered using status or date)
    @GetMapping("/all")
    public ResponseEntity<CommonResponse> getAllShipments() {
        CommonResponse cr = new CommonResponse();

        cr.data = shipmentRepository.findAll();
        cr.msg = "All shipments found";
        cr.status = HttpStatus.OK;

        return new ResponseEntity<>(cr, cr.status);
    }

    //    * GET/:customer_id (get all shipments by a customer)
    @GetMapping("/all/{account_id}")
    public ResponseEntity<CommonResponse> getAllShipmentsByAccount(@PathVariable Long account_id) {
        CommonResponse cr = new CommonResponse();

        Optional<Account> accountRepo = accountRepository.findById(account_id);
        Account account = accountRepo.orElse(null);

        cr.data = account.getShipments();
        cr.msg = "All shipments found for customer";
        cr.status = HttpStatus.OK;

        return new ResponseEntity<>(cr, cr.status);
    }

    //* GET/shipments by shipmentStatus
    @GetMapping("/status/{shipmentStatus}")
    public ResponseEntity<CommonResponse> getAllShipmentsByShipmentStatus(@PathVariable("shipmentStatus") Long shipmentStatus) {
        CommonResponse cr = new CommonResponse();
        try {
            ShipmentStatus statusType = ShipmentStatus.values()[shipmentStatus.intValue() - 1];
            cr.data = shipmentRepository.findAllByShipmentStatus(statusType);
            cr.msg = "List of all shipments with status: " + statusType;
            cr.status = HttpStatus.OK;
        } catch (Exception e) {
            cr.msg = "Unable to find any shipments with status code: " + shipmentStatus;
            cr.status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(cr, cr.status);
    }

    //    * GET/:customer_id/:shipment_id (get a specific shipment by a customer)
    @GetMapping("/{account_id}/{shipment_id}")
    public ResponseEntity<CommonResponse> getSpecificShipmentByCustomer(@PathVariable Long account_id, @PathVariable Long shipment_id) {
        CommonResponse cr = new CommonResponse();
        ShipmentDTO shipmentDTO = new ShipmentDTO();

        if (accountRepository.existsById(account_id)) {
            if (shipmentRepository.existsById(shipment_id)) {
                Optional<Shipment> shipmentRepo = shipmentRepository.findById(shipment_id);
                Shipment shipment = shipmentRepo.orElse(null);

                //   shipmentDTO.setAccountId(shipment.getAccount().getId());
                shipmentDTO.setBoxColour(shipment.getBoxColour());
                shipmentDTO.setDestinationCountry(shipment.getDestinationCountry());
                shipmentDTO.setWeight(shipment.getWeight());
                shipmentDTO.setReceiver(shipment.getReceiver());
                shipmentDTO.setShipmentId(shipment.getId());
                shipmentDTO.setSourceCountry(shipment.getSourceCountry());
                shipmentDTO.setShipmentStatus(shipment.getShipmentStatus());

                cr.status = HttpStatus.OK;
                cr.msg = "Specific shipment " + shipment_id + " for account " + account_id + " found";
                cr.data = shipmentDTO;
            } else {
                cr.status = HttpStatus.NOT_FOUND;
                cr.msg = "The specific shipment " + shipment_id + " could not be found.";
            }

        } else {
            cr.status = HttpStatus.NOT_FOUND;
            cr.msg = "The Specific account " + account_id + " could not be found.";
        }

        return new ResponseEntity<>(cr, cr.status);
    }

    //* GET/complete/:shipment_id (get details about specific completed shipment) NOT FINISHED
    @GetMapping("/{shipmentStatus}/{shipment_id}")
    public ResponseEntity<CommonResponse> getSpecificCompletedShipment(@PathVariable Long shipment_id, @PathVariable("shipmentStatus") Long shipmentStatus) {
        CommonResponse cr = new CommonResponse();
        List<Shipment> completedShipments;
        ShipmentDTO shipmentDTO = new ShipmentDTO();

        try {
            ShipmentStatus statusType = ShipmentStatus.values()[shipmentStatus.intValue() - 1];
            completedShipments = shipmentRepository.findAllByShipmentStatus(statusType);
            cr.data = "";
            cr.msg = "";
            cr.status = HttpStatus.OK;
        } catch (Exception e) {
            cr.msg = "Unable to find any shipments with status code: " + shipmentStatus;
            cr.status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(cr, cr.status);
    }

    /*
    TODO
    Create endpoints for:

    * GET/complete/:customer_id (get all complete shipments by a customer)

    Check so that these endpoints work:
    * GET/:customer_id/:shipment_id (get a specific shipment by a customer)
    */

}
