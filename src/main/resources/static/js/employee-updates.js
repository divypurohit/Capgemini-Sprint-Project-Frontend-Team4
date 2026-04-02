document.addEventListener("DOMContentLoaded", function() {
    const modalBody = document.querySelector('.modal-body');
    const updateBtns = document.querySelectorAll('.modal-body .btn-dark');

    // Helper to get the Employee ID from the modal title or a data attribute
    function getEmployeeId() {
        const titleElement = document.querySelector('.modal-title span');
        return titleElement ? titleElement.innerText : null;
    }


   // Global scope: HTML onclick="" can now see these functions

   function updateOfficeJS() {
       // 1. Get Employee ID directly from the browser URL (e.g., ?editId=1102)
       const urlParams = new URLSearchParams(window.location.search);
       const employeeId = urlParams.get('editId');

       // 2. Get Office ID from the input
       const officeIdInput = document.getElementById('officeIdInput');
       const officeId = officeIdInput ? officeIdInput.value : null;

       if (!employeeId || !officeId) {
           alert("Please enter a valid Office ID.");
           return;
       }

       fetch(`/updateOffice`, {
           method: 'PUT',
           headers: {
               'Content-Type': 'application/json'
           },
           body: JSON.stringify({
               id: employeeId,
               officeId: officeId
           })
       })
       .then(response => {
           if (response.ok) {
               response.json().then(data => {
                               window.location.href = '/?success=' + encodeURIComponent(data.message);
               });
           } else {
               // Read the error message sent from the backend (if any)
               response.text().then(text => alert("Error: " + (text || "Server rejected the update.")));
           }
       })
       .catch(err => console.error("Fetch Error:", err));
   }

   function updateManagerJS() {
       // 1. Get Employee ID from the browser URL
       const urlParams = new URLSearchParams(window.location.search);
       const employeeId = urlParams.get('editId');

       // 2. Get Manager ID from the input
       const managerIdInput = document.getElementById('managerIdInput');
       const managerId = managerIdInput ? managerIdInput.value : null;

       if (!employeeId || !managerId) {
           alert("Please enter a valid Manager ID.");
           return;
       }

       fetch(`/updateManager`, {
           method: 'PUT',
           headers: {
               'Content-Type': 'application/json'
           },
           body: JSON.stringify({
               id: employeeId,
               managerId: managerId
           })
       })
       // Removed the broken ".then(res" typo that was here
       .then(response => {
           if (response.ok) {
               window.location.href = '/?success=Manager+Updated';
           } else {
               response.text().then(text => alert("Error: " + (text || "Manager update failed.")));
           }
       })
       .catch(err => console.error("Fetch Error:", err));
   }

    if (updateBtns.length >= 3) {
        // Office Update Button
        updateBtns[1].addEventListener('click', function(e) {
            e.preventDefault();
            updateOfficeJS();
        });
        // Manager Update Button
        updateBtns[2].addEventListener('click', function(e) {
            e.preventDefault();
            updateManagerJS();
        });
    }
});