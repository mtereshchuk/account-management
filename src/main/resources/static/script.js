document.getElementById('createAccountForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const client = document.getElementById('client').value;
    const amount = document.getElementById('amount').value;
    const additionalInfo = document.getElementById('additionalInfo').value; // Get additional info

    fetch('/api/v1/accounts', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ client, amount: parseFloat(amount), additionalInfo }), // Include additional info
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.error || 'Failed to create account.'); // Use error field from response
            });
        }
        return response.json();
    })
    .then(data => {
        displayResult(`Account created: ${JSON.stringify(data)}`);
        document.getElementById('createAccountForm').reset();
        populateAccountDropdown(); // Update dropdown after creating an account
        populateDeleteAccountDropdown(); // Update delete dropdown after creating an account
        populateTransactionDropdowns(); // Update transaction dropdowns after creating an account
    })
    .catch(error => displayResult(`Error: ${error.message}`)); // Display error message
});

document.getElementById('loadAccounts').addEventListener('click', function() {
    fetch('/api/v1/accounts')
        .then(response => response.json())
        .then(data => {
            const accountList = document.getElementById('accountList');
            accountList.innerHTML = ''; // Clear previous results

            if (data.length === 0) {
                // Display a message if no accounts are found
                const li = document.createElement('li');
                li.textContent = 'No accounts available.';
                accountList.appendChild(li);
            } else {
                // Populate the list with account details
                data.forEach(account => {
                    const li = document.createElement('li');
                    li.textContent = `ID: ${account.id}, Client: ${account.client}, Amount: ${account.amount}, Additional Info: ${account.additionalInfo || 'N/A'}`; // Include additionalInfo
                    accountList.appendChild(li);
                });
            }
        })
        .catch(error => displayResult(`Error: ${error.message}`));
});

document.getElementById('transactionForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const type = document.getElementById('transactionType').value;
    const amount = document.getElementById('transactionAmount').value;
    const fromId = document.getElementById('fromId').value;
    const toId = document.getElementById('toId').value;

    let url = '';
    let body = {};

    if (type === 'deposit') {
        url = '/api/v1/transactions/deposit';
        body = { toId: parseInt(toId), amount: parseFloat(amount) };
    } else if (type === 'withdraw') {
        url = '/api/v1/transactions/withdraw';
        body = { fromId: parseInt(fromId), amount: parseFloat(amount) };
    } else if (type === 'transfer') {
        url = '/api/v1/transactions/transfer';
        body = { fromId: parseInt(fromId), toId: parseInt(toId), amount: parseFloat(amount) };
    }

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
    })
    .then(response => {
        // Check if the response is valid JSON
        if (!response.ok) {
            return response.text().then(text => {
                console.error('Error response:', text); // Log the error response
                throw new Error(text || 'Transaction failed'); // Use text response if JSON parsing fails
            });
        }
        
        // Handle empty response body
        if (response.status === 200 && response.headers.get('Content-Length') === '0') {
            return {}; // Return an empty object for empty response
        }

        return response.json(); // Return the JSON response if successful
    })
    .then(data => {
        // Check if data is empty and format the message accordingly
        const message = Object.keys(data).length === 0 
            ? 'Transaction successful' 
            : `Transaction successful: ${JSON.stringify(data)}`; // Only include data if it's not empty

        displayResult(message); // Display the returned message
        document.getElementById('transactionForm').reset();
    })
    .catch(error => displayResult(`Error: ${error.message}`)); // Display error message
});

// Add event listener for updating an account
document.getElementById('updateAccountForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const id = document.getElementById('updateAccountId').value; // Get the account ID from the input
    const client = document.getElementById('updateClient').value; // Get current client name
    const amount = document.getElementById('updateAmount').value; // Get current amount
    const additionalInfo = document.getElementById('updateAdditionalInfo').value; // Get additional info

    fetch(`/api/v1/accounts/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
            id: id, // Include the account ID
            client: client, // Include the current client name
            amount: parseFloat(amount), // Include the current amount
            additionalInfo: additionalInfo // Include the updated additional info
        }), // Send all fields
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.error || 'Failed to update account.'); // Use error field from response
            });
        }
        return response.json();
    })
    .then(data => {
        displayResult(`Account updated: ${JSON.stringify(data)}`);
        document.getElementById('updateAccountForm').reset();
        populateAccountDropdown(); // Update dropdown after updating an account
        populateDeleteAccountDropdown(); // Update delete dropdown after updating an account
    })
    .catch(error => displayResult(`Error: ${error.message}`));
});

// Add event listener for deleting an account
document.getElementById('deleteAccountForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const id = document.getElementById('deleteAccountId').value; // Get the account ID from the dropdown

    fetch(`/api/v1/accounts/${id}`, {
        method: 'DELETE',
    })
    .then(response => {
        if (response.ok) {
            displayResult(`Account with ID ${id} deleted successfully.`);
            populateAccountDropdown(); // Update dropdown after deleting an account
            populateDeleteAccountDropdown(); // Update delete dropdown after deleting an account
            populateTransactionDropdowns(); // Update transaction dropdowns after deleting an account
        } else {
            return response.json().then(err => {
                throw new Error(err.error || 'Failed to delete account.'); // Use error field from response
            });
        }
    })
    .catch(error => displayResult(`Error: ${error.message}`));
});

function displayResult(message) {
    const resultMessage = document.getElementById('resultMessage');
    resultMessage.textContent = message;
}

// Add this function to populate the dropdown with account IDs
function populateAccountDropdown() {
    fetch('/api/v1/accounts')
        .then(response => response.json())
        .then(data => {
            const accountDropdown = document.getElementById('updateAccountId');
            accountDropdown.innerHTML = '<option value="" disabled selected>Select Account ID</option>'; // Reset dropdown
            data.forEach(account => {
                const option = document.createElement('option');
                option.value = account.id; // Assuming account.id is the ID
                option.textContent = account.id; // Display the ID
                accountDropdown.appendChild(option);
            });
        })
        .catch(error => displayResult(`Error: ${error.message}`));
}

// Add this function to populate the dropdown with account IDs for deletion
function populateDeleteAccountDropdown() {
    fetch('/api/v1/accounts')
        .then(response => response.json())
        .then(data => {
            const deleteAccountDropdown = document.getElementById('deleteAccountId');
            deleteAccountDropdown.innerHTML = '<option value="" disabled selected>Select Account ID</option>'; // Reset dropdown
            data.forEach(account => {
                const option = document.createElement('option');
                option.value = account.id; // Assuming account.id is the ID
                option.textContent = account.id; // Display the ID
                deleteAccountDropdown.appendChild(option);
            });
        })
        .catch(error => displayResult(`Error: ${error.message}`));
}

// Add this function to populate the dropdowns for transactions
function populateTransactionDropdowns() {
    fetch('/api/v1/accounts')
        .then(response => response.json())
        .then(data => {
            const fromDropdown = document.getElementById('fromId');
            const toDropdown = document.getElementById('toId');

            fromDropdown.innerHTML = '<option value="" disabled selected>Select From Account ID</option>'; // Reset dropdown
            toDropdown.innerHTML = '<option value="" disabled selected>Select To Account ID</option>'; // Reset dropdown

            data.forEach(account => {
                const fromOption = document.createElement('option');
                fromOption.value = account.id; // Assuming account.id is the ID
                fromOption.textContent = account.id; // Display the ID
                fromDropdown.appendChild(fromOption);

                const toOption = document.createElement('option');
                toOption.value = account.id; // Assuming account.id is the ID
                toOption.textContent = account.id; // Display the ID
                toDropdown.appendChild(toOption);
            });
        })
        .catch(error => displayResult(`Error: ${error.message}`));
}

// Call this function when the page loads
document.addEventListener('DOMContentLoaded', function() {
    populateAccountDropdown(); // For update section
    populateDeleteAccountDropdown(); // For delete section
});

// Add event listener for when the account ID is selected
document.getElementById('updateAccountId').addEventListener('change', function() {
    const selectedId = this.value;
    fetch(`/api/v1/accounts/${selectedId}`)
        .then(response => response.json())
        .then(account => {
            document.getElementById('updateClient').value = account.client; // Set client name
            document.getElementById('updateAmount').value = account.amount; // Set amount
            document.getElementById('updateAdditionalInfo').value = account.additionalInfo; // Set additional info
        })
        .catch(error => displayResult(`Error: ${error.message}`));
});

// Add event listener for transaction type change
document.getElementById('transactionType').addEventListener('change', function() {
    const type = this.value;
    const fromIdField = document.getElementById('fromId');
    const toIdField = document.getElementById('toId');

    // Reset fields
    fromIdField.value = '';
    toIdField.value = '';

    // Show/hide fields based on transaction type
    if (type === 'deposit') {
        fromIdField.style.display = 'none'; // Hide fromId for deposit
        toIdField.style.display = 'block'; // Show toId for deposit
    } else if (type === 'withdraw') {
        fromIdField.style.display = 'block'; // Show fromId for withdraw
        toIdField.style.display = 'none'; // Hide toId for withdraw
    } else if (type === 'transfer') {
        fromIdField.style.display = 'block'; // Show fromId for transfer
        toIdField.style.display = 'block'; // Show toId for transfer
    }
});

// Initialize the visibility of fields on page load
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('transactionType').dispatchEvent(new Event('change')); // Trigger change event to set initial visibility
});