//для отображения таблицы карт с пагинацией.
async function getUserCards(page = 0) {
    const response = await fetch(`/api/user/cards?page=${page}&size=10`, {
        method: 'GET',
        credentials: 'include'
    });

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Operation failed');
    }

    const data = await response.json();
    console.log(`Total pages: ${data.totalPages}, cards: ${data.totalElements}`);

    const tBody = document.getElementById('cardsBody');
    if (tBody) {
        tBody.innerHTML = '';

        data.content.forEach(card => {
            const row = `<tr>
    <td>${card.maskedNumber}</td>
    <td>${card.expireDate}</td>
    <td>${card.cardStatus}</td>
    <td>${card.balance}</td>
    </tr>`;
            tBody.innerHTML += row;
        });
    }

    alert(`Loaded ${data.content.length} cards`);

    //кнопка паганации
    const paginationDiv = document.getElementById('pagination');
    paginationDiv.innerHTML = '';
    for (let i = 0; i < data.totalPages; i++) {
        const button = document.createElement('button');
        button.textContent = i + 1;
        button.onclick = () => getUserCards(i);
        if (i === page) button.disabled = true;
        paginationDiv.appendChild(button);
    }
}
//для отображения таблицы карт без пагинации
async function fetchUserCardsForSelect() {
    const response = await fetch(`/api/user/cards`, {
        credentials: 'include'
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Operation failed');
    }
    const data = await response.json();
    return data.content;
}

async function fetchUserCards() {
    const response = await fetch(`/api/user/cards?page=0&size=100`, {
         credentials: 'include'
    });
    if (!response.ok) throw new Error('Failed to load cards');
    const data = await response.json();
    return data.content;
}

async function requestBlockCard() {
    const cardId = document.getElementById('blockCardSelect').value;
    if (!cardId || cardId.trim() === '') {
        alert('Please, select card');
        return;
    }
    try {
        const response = await fetch(`/api/user/cards/${cardId}/block-request`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
        });
        if (response.ok) {
            alert('Block request complete');
            //обновить ui, чтобы не перезагружать страницу
            document.getElementById('blockStatus').textContent = 'Request PENDING';
            document.getElementById('blockButton').disabled = true;
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Operation failed'));
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    }
}


async function checkCardBalance() {
    const element = document.getElementById('balanceCardSelect');
    if (!element || !element.value) return;
    const cardId = element.value;

    const response = await fetch(`/api/user/cards/${cardId}/balance`, {
        method: 'GET',
        credentials: 'include',
    });
    if (response.ok) {
        const balance = await response.json();
        alert('Card balance: ' + balance);
        await getUserCards(0);//обновление страницы
    } else {
        const error = await response.json().catch(() => ({}));
        alert('Error: ' + (error.message || 'Get balance failed'));

    }
}

async function makeTransfer() {
    const fromCardId = document.getElementById('fromCardSelect').value;
    const toCardId = document.getElementById('toCardSelect').value;
    const amount = document.getElementById('amountInput').value;

    const response = await fetch('/api/user/cards/transfer', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            fromCardId: fromCardId,
            toCardId: toCardId,
            amount: amount
        })
    });

    if (response.ok) {
        alert('Transfer successful');
        document.getElementById('amountInput').value = '';
    } else {
        const error = await response.json();
        alert('Error: ' + (error.message || 'Transfer failed'));
    }
}

async function viewAllTransactions(page= 0) {
      try {
          const response = await fetch(`/api/user/transactions?page=${page}&size=10`, {
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

async function getBankStatementToCSV(page = 0) {
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';
    try {
        const response = await fetch(`/api/user/transactions/export?page=${page}&size=10`, {
            method: 'GET',
            credentials: 'include'
        });
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `transactions_${new Date().toISOString()}.csv`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    } finally {
        spinner.style.display = 'none';
    }
}

async function getBankStatementToPDF(page = 0) {
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';
    try {
        const response = await fetch(`/api/user/transactions/exportPDF?page=${page}&size=10`, {
            method: 'GET',
            credentials: 'include'
        });
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `transactions_${new Date().toISOString()}.pdf`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        }
    } catch (error) {
        alert('Network error: ' + error.message);
    } finally {
        spinner.style.display = 'none';
    }
}

//заполняем select в форме
async function userCardSelects() {
    try {
        const cards = await fetchUserCardsForSelect();
        const fromSelect = document.querySelector('[name="fromCardId"]');
        const toSelect = document.querySelector('[name="toCardId"]');
        cards.forEach(card => {
            addOptionToSelect(fromSelect, card);
            addOptionToSelect(toSelect, card);
        });
    } catch(error) {
        console.error('Failed to load cards for select:', e);
    }
}

function addOptionToSelect(selectElement, card) {
    const option = document.createElement('option');
    option.value = card.id;
    option.textContent = card.maskedNumber;
    selectElement.appendChild(option);
}

//заполнение селектов при загрузке страницы и загрузк первой страницы таблицы
window.onload = async function () {
    try {
        await userCardSelects();
        await getUserCards(0);
    } catch (e) {
        console.error('Failed to load page data:', e);
    }
}