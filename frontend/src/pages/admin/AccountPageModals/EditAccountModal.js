import React, { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import Modal from '../../../components/modal/index';
import { parseISO } from 'date-fns';

import "./../style.scss";

import { useAuth } from "../../../context/auth";
import { ADMIN, USER } from '../../../utils/roles';
import { updateAccount } from '../../../api/user';
import DatePicker from 'react-datepicker';

import "react-datepicker/dist/react-datepicker.css";


const EditAccountModal = (props) => {

    const auth = useAuth();
    const { register, handleSubmit, errors, watch, reset } = useForm();
    const [showModal, setShowModal] = useState(true);
    const [dob, setDOB] = useState(new Date());

    useEffect(() => {
        !props.thisAccount.dateOfBirth ? setDOB(new Date()) : setDOB(parseISO(props.thisAccount.dateOfBirth)); // If user does not have a chosen DoB, set a temporary one
        reset({...props.thisAccount})
    }, [reset])

    const onSubmit = (data) => {
        
        if (props.thisAccount.firstName === data.firstName) {
            delete data.firstName;
        }
        
        if (props.thisAccount.lastName === data.lastName) {
            delete data.lastName;
        }

        if (props.thisAccount.email === data.email) {
            delete data.email;
        }

        if (props.thisAccount.zipCode === parseInt(data.zipCode)) {
            delete data.zipCode;
        }

        if (props.thisAccount.contactNumber === parseInt(data.contactNumber)) {
            delete data.contactNumber;
        }

        if (props.thisAccount.country === data.country) {
            delete data.country;
        } 

        if (props.thisAccount.role === data.role) {
            delete data.role;
        }

        let formData = data;

        if (parseISO(props.thisAccount.dateOfBirth) === formData.dateOfBirth) {
            delete formData.dateOfBirth;
        } else {
            formData.dateOfBirth = dob;
        }

        console.log(formData);

        handleSaveEditedUser(formData);
        
    };


    const handleSaveEditedUser = async (user) => { // Called when an admin saves changes to an account
        try {
            const token = await auth.getUserToken(); // Get sessiontoken

            await updateAccount(token, props.thisAccount.id, user); // Pass token, pathvariable and body with request
            props.reRender(); // Rerender page
            props.toggleToast("saved");
        } catch (error) {
            console.log(error);
        } finally { // popup closed
            setShowModal(!showModal);
            props.onClose();
        }
    }

    const onClose = () => {
        setShowModal(!showModal);
        props.onClose();
    }

    return (
        <>
            <Modal isVisible={showModal} onClose={onClose}>
                <h3>Editing {props.thisAccount.firstName} {props.thisAccount.lastName}</h3>
                <form onSubmit={handleSubmit(onSubmit)} className="edit-account-form">
                    <div className="input-row">
                        <label htmlFor="firstName">Firstname: </label>
                        <input
                            name="firstName"
                            defaultValue={props.thisAccount.firstName} 
                            ref={register({
                                pattern: {
                                    value: /^[A-Za-z]+$/,
                                    message: "invalid format"
                                }
                            })}>
                        </input>
                    </div>
                    <div className="input-row">
                        <label htmlFor="lastName">Lastname: </label>
                        <input 
                            type="text" 
                            name="lastName" 
                            defaultValue={props.thisAccount.lastName} 
                            ref={register({
                                pattern: {
                                    value: /^[A-Za-z]+$/,
                                    message: "invalid format"
                                }
                            })}>
                        </input>
                    </div>
                    <div className="input-row">
                        <label htmlFor="email">Email: </label>
                        <input 
                            type="text" 
                            name="email" 
                            defaultValue={props.thisAccount.email} 
                            ref={register({
                                pattern: {
                                    value: /\S+@\S+\.\S+/,
                                    name: "invalid format"
                                }
                            })}>
                        </input>
                    </div>
                    <div className="input-row">
                        <label htmlFor="dateOfBirth">Date of birth: </label>
                        <DatePicker
                            id="dateOfBirth"
                            showYearDropdown
                            selected={dob}
                            dateFormat="dd/MM/yyyy"
                            maxDate={new Date()}
                            onChange={(date) => setDOB(date)}
                            placeholderText="DD/MM/YYYY"
                            autoComplete="off"
                        />
                        
                    </div>
                    <div className="input-row">
                        <label htmlFor="zipCode">Zip code: </label>
                        <input 
                            type="text" 
                            name="zipCode"
                            defaultValue={props.thisAccount.zipCode}
                            ref={register}>
                        </input>
                    </div>
                    <div className="input-row">
                        <label htmlFor="country">Country: </label>
                        <select
                            ref={register}
                            name="country"
                        >
                            {!props.countries ? "loading..." :
                            props.countries.map((country, index) => {
                                return <option key={index} value={country}>{country}</option>
                            })}
                        </select>
                    </div>
                    <div className="input-row">
                        <label htmlFor="contactNumber">Contact number: </label>
                        <input 
                            type="text"
                            name="contactNumber"
                            defaultValue={props.thisAccount.contactNumber}
                            ref={register}>
                        </input>
                    </div>
                    <div className="input-row">
                        <label htmlFor="role">Role: </label>
                        <select
                            placeholder={props.thisAccount.role}
                            name="role"
                            ref={register}>
                                <option value={USER}>USER</option>
                                <option value={ADMIN}>ADMIN</option>
                        </select>
                    </div>
                    <div className="form-buttons">
                        <input type="submit" className="btn btn-primary" value="Save" />
                        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
                    </div>
                </form>
            </Modal>
        </>
    )
} 

export default EditAccountModal;