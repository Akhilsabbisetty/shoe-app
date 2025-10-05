fetch('https://api.shoes.akhilsabbisetty.site/shoes')
    .then(response => response.json())
    .then(data => {
        const appDiv = document.getElementById('app');
        data.forEach(shoe => {
            const el = document.createElement('p');
            el.innerText = `${shoe.name} - ${shoe.price}`;
            appDiv.appendChild(el);
        });
    })
    .catch(err => console.error(err));
