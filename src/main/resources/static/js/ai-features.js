const aiDraftAssistant = document.getElementById('aiDraftAssistant');

if (aiDraftAssistant) {
    const draftButton = document.getElementById('generateDonationDraft');
    const notesInput = document.getElementById('aiNotes');
    const status = document.getElementById('aiDraftStatus');

    draftButton.addEventListener('click', async () => {
        const notes = notesInput.value.trim();
        if (!notes) {
            status.textContent = 'Add a few notes about the item first.';
            return;
        }

        draftButton.disabled = true;
        status.textContent = 'Creating a draft…';
        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
            const headers = {'Content-Type': 'application/json'};
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

            const response = await fetch(aiDraftAssistant.dataset.suggestUrl, {
                method: 'POST',
                credentials: 'same-origin',
                headers,
                body: JSON.stringify({notes})
            });
            const result = await response.json().catch(() => ({}));
            if (!response.ok) {
                throw new Error(result.message || 'AI drafting is unavailable. Please try again.');
            }

            document.getElementById('title').value = result.title;
            document.getElementById('description').value = result.description;
            document.getElementById('category').value = result.category;
            const missingDetails = Array.isArray(result.missingDetails) ? result.missingDetails : [];
            status.textContent = missingDetails.length
                ? `Draft added. Please review it and add details such as: ${missingDetails.join(', ')}.`
                : 'Draft added. Review and edit it before publishing.';
        } catch (error) {
            status.textContent = error.message || 'AI drafting is unavailable. Please try again.';
        } finally {
            draftButton.disabled = false;
        }
    });
}
