import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.bizsync.ui.R

@Composable
fun ImageUi(photoUrl : Uri?) {

    AsyncImage(
        model = photoUrl ?: R.drawable.default_profile_picture, // Usa un'immagine di default se non c'è una foto
        contentDescription = "Profilo dell'utente",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape), // Per rendere l'immagine rotonda
        contentScale = ContentScale.Crop
    )
}
