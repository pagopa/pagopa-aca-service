module.exports = (req, res, next) => {
  console.log(`${new Date().toISOString()} - Received request: ${req.method} ${req.path} - Body:  ${JSON.stringify(req.body)}`)
  req.method = 'GET'
  next()
}