package dev.secam.simpletag.ui.selector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.theme.SimpleTagTheme

@Composable
fun PermissionScreen(modifier: Modifier = Modifier, onClick: () -> Unit){
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .offset(y= (-60).dp)
            .padding(horizontal = 24.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_music_note_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(48.dp)

        )
        Text(
            text = stringResource(R.string.welcome),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .padding(bottom = 6.dp)
        )
        Text(
            text = stringResource(R.string.missing_permission),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Button(
            onClick = onClick,
            modifier = Modifier

        ) {
            Text(
                text = stringResource(R.string.grant_permisson),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun PermissionPrev(){
    SimpleTagTheme {
        PermissionScreen {  }
    }
}