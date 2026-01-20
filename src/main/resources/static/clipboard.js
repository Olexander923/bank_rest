const copyTextToClipboard = async(text) => {
    try {
        await navigator.clipboard.writeText(text);
        console.log('text successfully copied!');
    } catch (err) {
        console.log('Error: ',err);
    }
}

function copyToken() {
    const jwtToken = document.getElementsByName('jwtToken').values();
    copyTextToClipboard(jwtToken);
    
}