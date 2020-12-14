import React, { useState } from 'react';
import Modal from '../../../components/modal/index';

import { useAuth } from '../../../context/auth';

import { deleteAccount } from '../../../api/user';

const DeleteUserModal = (props) => {

    const auth = useAuth();
    const [showModal, setShowModal] = useState(true);
    const [deleteBtn, setDeleteBtn] = useState(false);

    const deleteUser = async (user) => {
        try {
            const token = await auth.getUserToken(); // Get sessiontoken

            await deleteAccount(token, user.id); // Pass token and pathvariable

        } catch (error) {
            console.log(error);
        } finally {
            onClose();
        }
    }

    const validateInput = (input) => {
        if(input === props.thisUser.email) setDeleteBtn(!deleteBtn);
    }

    const onClose = () => {
        setShowModal(!showModal);
    }

    return (
        <>
            <Modal isVisible={showModal} onClose={onClose}>
                <p>Are you sure you want to delete the account with id: {props.thisUser.id}?
                    Provide this phrase to confirm delete: <strong>{props.thisUser.email}</strong></p>
                <form>
                    <input type="text" onChange={(event) => validateInput(event.target.value)}/>
                    <button disabled={!deleteBtn}type="button" onClick={() => deleteUser(props.thisUser)}>Delete User</button>
                    <button type="button" onClick={onClose}>Cancel</button>
                </form>
            </Modal>
        </>
    )
}

export default DeleteUserModal;