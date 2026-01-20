async function createCard() {
    const data = {
        userId: document.querySelector('[name="userId"]').value,
        cardNumber: document.querySelector('[name="cardNumber"]').value,
        expireDate: document.querySelector('[name="expireDate"]').value,
        cardStatus: document.querySelector('[name="cardStatus"]').value,
        balance: document.querySelector('[name="balance"]').value,
    };
    try {
        const response = await fetch(`/api/admin/cards`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        if (response.ok) {
            alert('Card successfully created');
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Operation failed'));
        }
    } catch(error) {
        alert('Network error: ' + error.message);
    }
}

async function blockCard() {
    const cardId = document.getElementById('blockCardSelect').value;
    if (!cardId || cardId.trim() === '') {
        alert('Please, select card');
        return;
    }
    try {
        const response = await fetch(`/api/admin/cards/${cardId}/block`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
        });
        if (response.ok) {
            alert('Card was blocked');
            document.getElementById('blockStatus').textContent = 'BLOCK';
            document.getElementById('blockButton').disabled = true;
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Operation failed'));
        }

    } catch (error) {
        alert('Network error: ' + error.message);
    }
}

async function activateCard() {
    const cardId = document.getElementById('activateCardSelect').value;
    if (!cardId || cardId.trim() === '') {
        alert('Please, select card');
        return;
    }
    try {
        const response = await fetch(`/api/admin/cards/${cardId}/activate`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
        });
        if (response.ok) {
            alert('Card was activated');
            document.getElementById('cardStatus').textContent = 'ACTIVE';

            //карту удалить из списка задлокированных
            const blockSelect = document.getElementById('blockCardSelect');
            const option = blockSelect.querySelector(`option[value="${cardId}"]`);
            if (option) option.remove();
            document.getElementById('activateCardSelect').value = '';
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Operation failed'));
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    }
}

async function getAllUserTransactions(page = 0) {
    const userId = document.getElementById('transactionSelect').value;
    if (!userId || userId.trim() === '') {
        return;
    }
    //const token = localStorage.getItem('jwt');
    try {
        const response = await fetch(`/api/admin/transactions?userId=${userId}&page=${page}&size=10`, {
            method: 'GET',
            credentials: 'include'
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Operation failed');
        }
        const data = await response.json();
        console.log(`Total pages: ${data.totalPages}, cards: ${data.totalElements}`);

        const tBody = document.getElementById('transactionsBody');
        tBody.innerHTML = '';
        data.content.forEach(transaction => {
            const row = `<tr>
    <td>${transaction.fromMaskedCard}</td>
    <td>${transaction.toMaskedCard}</td>
    <td>${transaction.amount}</td>
    <td>${transaction.timeStamp}</td>
    <td>${transaction.transactionStatus}</td>
    </tr>`;
            tBody.innerHTML += row;
        });
        alert(`Loaded ${data.content.length} transactions`);
    } catch (error) {
        alert('Network error: ' + error.message);
    }
}

//заполнение select в форме
async function cardSelects() {
    const response = await fetch('/api/admin/cards', {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
    });
    const data = await response.json();
    const cardArray = data.content;
    cardArray.forEach(card => {
        addOptionToSelect(blockSelect, card);
        addOptionToSelect(activateSelect, card);
    });
}

//заполнение селектов при загрузке страницы
window.onload = function () {
    cardSelects();
    populateUserSelect();
}

//заполнять список пользователей, вспомогательная функция
async function populateUserSelect() {
    const response = await fetch(`/api/admin/users`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
    });
    const users = await response.json();
    users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.id;
        option.textContent = user.username;
        transactionSelect.appendChild(option);
    });
}