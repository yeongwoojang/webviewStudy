let express = require('express');
let app = express();

const path = require('path');
app.use(express.static(path.join(__dirname,'../','public')));
app.get('/',(req,res)=>{
 res.sendFile(path.join(__dirname,'../','index.html'));
});

app.listen(3333,function(){
    console.log('Server is running...')
});
